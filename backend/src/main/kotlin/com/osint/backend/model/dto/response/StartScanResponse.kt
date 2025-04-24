package com.osint.backend.model.dto.response

import com.osint.backend.model.enums.ScanStatus
import java.util.UUID

data class StartScanResponse(
    val id: UUID,
    val domain: String,
    val status: ScanStatus,
    val message: String
)
