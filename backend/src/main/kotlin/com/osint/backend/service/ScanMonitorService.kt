package com.osint.backend.service

import com.osint.backend.model.domain.ScanEntity
import com.osint.backend.model.enums.ScanStatus
import com.osint.backend.properties.ScanProperties
import com.osint.backend.repository.ScanRepository
import com.osint.backend.util.ScanNamingUtils
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

private val log = KotlinLogging.logger {}

@Service
class ScanMonitorService(
    private val repository: ScanRepository,
    private val dockerExecutor: DockerExecutor,
    private val scanProperties: ScanProperties,
    private val scanService: ScanService,
) {

    @Scheduled(fixedDelay = 60_000)
    fun monitorScans() {
        handleRunningScans()
        processQueuedScans()
    }

    private fun handleRunningScans() {
        val inProgressScans = repository.findAllByStatus(ScanStatus.IN_PROGRESS)

        inProgressScans.forEach { scan ->
            val containerName = ScanNamingUtils.getContainerName(scan.id, scan.domain)

            val status = dockerExecutor.getContainerStatus(containerName)
            when (status) {
                ContainerStatus.NOT_EXIST -> handleNotExist(scan)
                ContainerStatus.STOPPED -> handleStopped(scan, containerName)
                ContainerStatus.RUNNING -> handleRunning(scan, containerName)
                ContainerStatus.UNKNOWN -> {}
            }
        }
    }

    private fun isTimeoutReached(scan: ScanEntity): Boolean {
        val timeout = scan.timeoutMinutes
        if (timeout == null || timeout <= 0L) return false

        val startTime = scan.startedAt ?: return false
        return Instant.now().isAfter(startTime.plus(timeout, ChronoUnit.MINUTES))
    }

    private fun handleStopped(scan: ScanEntity, containerName: String) {
        val content = dockerExecutor.extractScanOutput(containerName)
        val normalizedContent = content.trim()
        val result = normalizedContent.ifEmpty { "[NO OUTPUT]" }

        val status = when {
            normalizedContent.isBlank() -> ScanStatus.EMPTY_RESULT
            normalizedContent.contains("No assets were discovered", ignoreCase = true) -> ScanStatus.EMPTY_RESULT
            else -> ScanStatus.SUCCESS
        }

        scanService.finalize(scan.domain, status, result)

        val containerDeleted = dockerExecutor.destroyContainer(containerName)

        if (containerDeleted) {
            log.info { "Successfully deleted container: $containerName" }
        } else {
            log.warn { "Failed to delete container: $containerName" }
        }
    }

    private fun handleRunning(scan: ScanEntity, containerName: String) {
        if (!isTimeoutReached(scan)) {
            return
        }

        val content = dockerExecutor.extractScanOutput(containerName)
        val result = content.ifBlank { "[NO OUTPUT]" }

        scanService.finalize(scan.domain, ScanStatus.TIMEOUT, result)

        val containerDeleted = dockerExecutor.destroyContainer(containerName)

        if (containerDeleted) {
            log.info { "Successfully deleted container: $containerName" }
        } else {
            log.warn { "Failed to delete container: $containerName" }
        }
    }

    private fun handleNotExist(scan: ScanEntity) {
        val result = "[AN ERROR OCCURRED AND NO SCAN OUTPUT IS AVAILABLE]"

        log.error { "Failure: Scan for domain ${scan.domain} could not complete. $result" }

        scanService.finalize(scan.domain, ScanStatus.FAILED, result)
    }

    private fun processQueuedScans() {
        val running = dockerExecutor.getRunningContainerCount()
        val maxAllowed = scanProperties.maxActiveContainers
        val slotsAvailable = (maxAllowed - running).coerceAtLeast(0)

        if (slotsAvailable == 0) {
            log.info { "No available slots to process queued scans. Active containers: $running, Max allowed: $maxAllowed" }
            return
        }

        val pageable = PageRequest.of(0, slotsAvailable)
        val scansToRun = repository.findAllByStatusOrderByCreatedAtAsc(ScanStatus.QUEUED, pageable)

        scansToRun.forEach { scan ->
            try {
                log.info { "Launching scan for domain: ${scan.domain}" }
                scanService.launchScan(scan.domain)
            } catch (ex: Exception) {
                log.error(ex) { "Failed to launch scan for domain: ${scan.domain}. Error: ${ex.message}" }
            }
        }
    }
}
