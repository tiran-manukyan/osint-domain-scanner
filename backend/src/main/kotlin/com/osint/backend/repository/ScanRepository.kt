package com.osint.backend.repository

import com.osint.backend.model.domain.ScanEntity
import com.osint.backend.model.enums.ScanStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ScanRepository : JpaRepository<ScanEntity, UUID> {

    fun findByDomainAndStatus(domain: String, status: ScanStatus): Optional<ScanEntity>

    fun findTopByDomainOrderByStartedAtDesc(domain: String): ScanEntity?

    fun findAllByStatusOrderByCreatedAtAsc(status: ScanStatus, pageable: Pageable): List<ScanEntity>

    fun findAllByStatus(status: ScanStatus): List<ScanEntity>

}