package com.osint.backend.service

import com.osint.backend.model.enums.ContainerStatus
import com.osint.backend.util.ScanNamingUtils
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

@Component
class DockerExecutor {

    @Async("scanExecutor")
    fun runDetachedScan(domain: String, scanId: UUID) {
        val containerName = ScanNamingUtils.getContainerName(scanId, domain)

        val command = listOf(
            "docker", "run", "-d",
            "--name", containerName,
            "--label", "com.osint.scan=true",
            "--label", "com.osint.scan.id=$scanId",
            "caffix/amass",
            "enum", "-d", domain
        )

        ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
            .also { log.info { "Started Docker container: $containerName" } }
    }

    fun extractScanOutput(containerName: String): String = runCatching {
        ProcessBuilder("docker", "logs", containerName)
            .redirectErrorStream(true)
            .start()
            .inputStream
            .bufferedReader()
            .use { it.readText().trim() }
    }.onFailure { e ->
        log.error(e) { "Failed to get logs from container '$containerName'" }
    }.getOrDefault("[ERROR] Failed to retrieve logs")

    fun destroyContainer(containerName: String): Boolean = runCatching {
        ProcessBuilder("docker", "rm", "-f", containerName)
            .redirectErrorStream(true)
            .start()
            .apply {
                if (!waitFor(10, TimeUnit.SECONDS)) {
                    destroyForcibly()
                    log.warn { "Timeout while removing container: $containerName" }
                    return false
                }
            }.exitValue()
    }.fold(
        onSuccess = { exitCode ->
            when (exitCode) {
                0 -> {
                    log.info { "Successfully removed container: $containerName" }
                    true
                }

                else -> {
                    log.error { "Docker exited with code $exitCode while removing container: $containerName" }
                    false
                }
            }
        },
        onFailure = { e ->
            log.error(e) { "Exception while removing container '$containerName'" }
            false
        }
    )

    fun getContainerStatus(containerName: String): ContainerStatus {
        return runCatching {
            val process = ProcessBuilder(
                "docker", "inspect", "-f", "{{.State.Status}}", containerName
            )
                .redirectErrorStream(false)
                .start()

            val finished = process.waitFor(10, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                log.warn("Timeout while getting the container status: {}", containerName)
                return ContainerStatus.UNKNOWN
            }

            val error = process.errorStream.bufferedReader().readText().trim()
            if (error.contains("No such object", ignoreCase = true)) {
                return ContainerStatus.NOT_EXIST
            }

            val status = process.inputStream.bufferedReader().readText().trim()
            when (status) {
                "running" -> ContainerStatus.RUNNING
                "exited" -> ContainerStatus.STOPPED
                else -> {
                    log.warn { "Container status check returned unexpected output: Container '$containerName', Status: $status" }
                    ContainerStatus.UNKNOWN
                }
            }
        }.getOrElse { e ->
            log.error(e) { "Failed to get container status for: $containerName" }
            ContainerStatus.UNKNOWN
        }
    }

    fun getRunningContainerCount(): Int = runCatching {
        ProcessBuilder(
            "docker", "ps", "-q",
            "--filter", "label=com.osint.scan=true"
        )
            .redirectErrorStream(true)
            .start()
            .inputStream
            .bufferedReader()
            .useLines { it.count() }
            .also { count -> log.info { "Found $count running Docker containers with label com.osint.scan=true" } }
    }.getOrElse { e ->
        log.error(e) { "Failed to check Docker containers" }
        throw IllegalStateException("Failed to check Docker containers", e)
    }
}
