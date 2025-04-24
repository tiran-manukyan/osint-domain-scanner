package com.osint.backend.model.enums

enum class ScanStatus {
    QUEUED,
    IN_PROGRESS,
    SUCCESS,
    EMPTY_RESULT,
    FAILED,
    TIMEOUT
}