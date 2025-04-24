package com.osint.backend.model.dto.request

data class ScanRequest(
    val domain: String,
    val timeoutMinutes: Long? = null
)