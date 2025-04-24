package com.osint.backend.service

import com.osint.backend.util.ScanNamingUtils
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

@Component
class DockerExecutor(
) {
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
    }

    fun extractScanOutput(containerName: String): String {
        return try {
            val process = ProcessBuilder("docker", "logs", containerName)
                .redirectErrorStream(true)
                .start()

            process.inputStream.bufferedReader().readText().trim()
        } catch (e: Exception) {
            log.error(e) { "Failed to get logs from container '$containerName'" }
            "[ERROR] Failed to retrieve logs"
        }
    }


    fun destroyContainer(containerName: String): Boolean {
        return try {
            val process = ProcessBuilder("docker", "rm", "-f", containerName)
                .redirectErrorStream(true)
                .start()

            val finished = process.waitFor(10, TimeUnit.SECONDS)

            if (!finished) {
                process.destroyForcibly()
                log.warn("Timeout while removing container: {}", containerName)
                return false
            }

            val exitCode = process.exitValue()
            if (exitCode == 0) {
                log.info("Successfully removed container: {}", containerName)
                true
            } else {
                log.error("Docker exited with code {} while removing container: {}", exitCode, containerName)
                false
            }
        } catch (e: Exception) {
            log.error("Exception while removing container '{}': {}", containerName, e.message, e)
            false
        }
    }

    fun getContainerStatus(containerName: String): ContainerStatus {
        val process = ProcessBuilder(
            "docker", "inspect", "-f", "{{.State.Status}}", containerName
        ).start()

        val finished = process.waitFor(10, TimeUnit.SECONDS)

        if (!finished) {
            process.destroyForcibly()
            log.warn("Timeout while getting the container status: {}", containerName)
            return ContainerStatus.UNKNOWN
        }

        val status = process.inputStream.bufferedReader().readText().trim()
        val error = process.errorStream.bufferedReader().readText().trim()
        if (error.contains("No such object", ignoreCase = true)) {
            return ContainerStatus.NOT_EXIST
        }

        if (status == "running") {
            return ContainerStatus.RUNNING
        }
        else if (status == "exited") {
            return ContainerStatus.STOPPED
        }

        log.warn { "Container status check returned unexpected output: Container '$containerName', Status: $status" }

        return ContainerStatus.UNKNOWN
    }

    fun getRunningContainerCount(): Int {
        return try {
            val countCommand = listOf(
                "docker", "ps", "-q",
                "--filter", "label=com.osint.scan=true"
            )

            val process = ProcessBuilder(countCommand)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readLines()
            val count = output.count()

            log.info { "Found $count running Docker containers with label com.osint.scan=true" }

            count
        } catch (e: Exception) {
            log.error(e) { "Failed to check Docker containers" }
            throw IllegalStateException("Failed to check Docker containers", e)
        }
    }
}
