package com.osint.backend.model.dto.response

import com.osint.backend.model.enums.ScanStatus
import java.time.Instant
import java.util.UUID

data class ScanResponse(
    val id: UUID,
    val domain: String,
    val status: ScanStatus,
    val result: String?,
    val startedAt: Instant?,
    val finishedAt: Instant?,
)
