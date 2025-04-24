package com.osint.backend.model.domain

import com.osint.backend.model.enums.ScanStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "scans", schema = "osint")
data class ScanEntity(

    @Id
    val id: UUID,

    val domain: String,

    @Enumerated(EnumType.STRING)
    var status: ScanStatus,

    @Column(columnDefinition = "TEXT")
    var result: String? = null,

    var timeoutMinutes: Long? = null,

    var startedAt: Instant? = null,

    var finishedAt: Instant? = null,

    val createdAt: Instant = Instant.now(),

    var updatedAt: Instant = Instant.now()
)