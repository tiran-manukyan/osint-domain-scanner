package com.osint.backend.service

import DomainUtils
import com.osint.backend.model.domain.ScanEntity
import com.osint.backend.model.dto.request.ScanRequest
import com.osint.backend.model.dto.response.ScanResponse
import com.osint.backend.model.dto.response.StartScanResponse
import com.osint.backend.model.enums.ScanStatus
import com.osint.backend.repository.ScanRepository
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
        val domain = DomainUtils.normalize(request.domain)

        val lock = acquireLock(domain)
        try {
            val existing = repository.findTopByDomainOrderByStartedAtDesc(domain)
            if (existing != null) {
                if (existing.status == ScanStatus.IN_PROGRESS) {
                    return existing.toStartScanResponse(message = "Scan is already in progress.")
                } else if (existing.status == ScanStatus.QUEUED) {
                    return existing.toStartScanResponse(message = "Scan is already queued.")
                }
            }

            val scanEntity = ScanEntity(
                id = UUID.randomUUID(),
                domain = domain,
                status = ScanStatus.QUEUED,
                timeoutMinutes = request.timeoutMinutes,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            repository.save(scanEntity)

            return scanEntity.toStartScanResponse("Scan is queued and will be started soon.")
        } finally {
            releaseLock(domain, lock)
        }
    }

    fun getAll(pageable: Pageable): Page<ScanResponse> =
        repository.findAll(pageable)
            .map { it.toResponse() }

    fun launchScan(domain: String) {
        val lock = domainLocks.computeIfAbsent(domain, { it -> ReentrantLock() })

        acquireLock(domain)
        try {
            val scan = repository.findByDomainIsAndStatusIs(domain, ScanStatus.QUEUED)
                .orElseThrow { NoSuchElementException("No queued scan found for domain $domain") }
            scan.status = ScanStatus.IN_PROGRESS
            scan.startedAt = Instant.now()

            repository.save(scan)

            dockerExecutor.runDetachedScan(scan.domain, scan.id)
        } finally {
            releaseLock(domain, lock)
        }
    }

    fun finalize(domain: String, status: ScanStatus, result: String) {
        if (status !in listOf(ScanStatus.SUCCESS, ScanStatus.FAILED, ScanStatus.TIMEOUT, ScanStatus.EMPTY_RESULT)) {
            throw IllegalArgumentException("Invalid finalize status: $status")
        }

        val lock = domainLocks.computeIfAbsent(domain, { it -> ReentrantLock() })

        acquireLock(domain)
        try {
            val scan = repository.findByDomainIsAndStatusIs(domain, ScanStatus.IN_PROGRESS)
                .orElseThrow { NoSuchElementException("No in-progress scan found for domain $domain") }
            scan.status = status
            scan.result = result
            scan.finishedAt = Instant.now()

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
