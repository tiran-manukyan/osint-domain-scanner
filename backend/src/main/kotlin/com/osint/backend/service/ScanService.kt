package com.osint.backend.service

import com.osint.backend.model.domain.ScanEntity
import com.osint.backend.model.dto.request.ScanRequest
import com.osint.backend.model.dto.response.ScanResponse
import com.osint.backend.model.dto.response.StartScanResponse
import com.osint.backend.model.enums.ScanStatus
import com.osint.backend.repository.ScanRepository
import com.osint.backend.util.DomainUtils.normalizeDomain
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Service
class ScanService(
    private val repository: ScanRepository,
    private val dockerExecutor: DockerExecutor
) {

    private val domainLocks: ConcurrentHashMap<String, ReentrantLock> = ConcurrentHashMap()

    fun startScan(request: ScanRequest): StartScanResponse {
        val domain = request.domain.normalizeDomain()

        val lock = acquireLock(domain)
        try {
            repository.findTopByDomainOrderByStartedAtDesc(domain)
                ?.let { existing ->
                    if (existing.status == ScanStatus.IN_PROGRESS) {
                        return existing.toStartScanResponse(message = "Scan is already in progress.")
                    } else if (existing.status == ScanStatus.QUEUED) {
                        return existing.toStartScanResponse(message = "Scan is already queued.")
                    }
                }

            val now = Instant.now()

            val scanEntity = ScanEntity(
                id = UUID.randomUUID(),
                domain = domain,
                status = ScanStatus.QUEUED,
                timeoutMinutes = request.timeoutMinutes,
                createdAt = now,
                updatedAt = now
            )

            repository.save(scanEntity)

            return scanEntity.toStartScanResponse("Scan is queued and will be started soon.")
        } finally {
            releaseLock(domain, lock)
        }
    }

    fun getAll(pageable: Pageable): Page<ScanResponse> =
        repository.findAll(pageable).map { it.toResponse() }

    fun launchScan(domain: String) {
        val lock = domainLocks.computeIfAbsent(domain) { ReentrantLock() }

        acquireLock(domain)
        try {
            val scan = repository.findByDomainAndStatus(domain, ScanStatus.QUEUED)
                .orElseThrow { NoSuchElementException("No queued scan found for domain $domain") }

            val now = Instant.now()

            scan.apply {
                status = ScanStatus.IN_PROGRESS
                startedAt = now
                updatedAt = now
            }

            repository.save(scan)
            dockerExecutor.runDetachedScan(scan.domain, scan.id)
        } finally {
            releaseLock(domain, lock)
        }
    }

    fun finalize(domain: String, status: ScanStatus, result: String) {
        require(
            status in setOf(
                ScanStatus.SUCCESS,
                ScanStatus.FAILED,
                ScanStatus.TIMEOUT,
                ScanStatus.EMPTY_RESULT
            )
        ) { "Invalid finalize status: $status" }

        val lock = acquireLock(domain)
        try {
            val scan = repository.findByDomainAndStatus(domain, ScanStatus.IN_PROGRESS)
                .orElseThrow { NoSuchElementException("No in-progress scan found for domain $domain") }

            val now = Instant.now()

            scan.apply {
                this.status = status
                this.result = result
                finishedAt = now
                updatedAt = now
            }

            repository.save(scan)
        } finally {
            releaseLock(domain, lock)
        }
    }

    private fun acquireLock(domain: String): ReentrantLock {
        val lock = domainLocks.computeIfAbsent(domain, { it -> ReentrantLock() })

        lock.lock()

        if (domainLocks.containsKey(domain)) {
            return lock
        } else {
            return acquireLock(domain)
        }
    }

    private fun releaseLock(domain: String, lock: ReentrantLock) {
        if (!lock.hasQueuedThreads()) {
            domainLocks.remove(domain)
        }

        lock.unlock()
    }

    private fun ScanEntity.toResponse() = ScanResponse(
        id = id,
        domain = domain,
        status = status,
        result = result,
        startedAt = startedAt,
        finishedAt = finishedAt
    )

    private fun ScanEntity.toStartScanResponse(message: String) = StartScanResponse(
        id = id,
        domain = domain,
        status = status,
        message = message
    )
}
