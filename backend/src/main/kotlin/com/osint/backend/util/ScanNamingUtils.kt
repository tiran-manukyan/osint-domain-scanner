package com.osint.backend.util

import java.util.*

object ScanNamingUtils {

    fun getContainerName(scanId: UUID, domain: String): String =
        "osint-scan-$domain-$scanId"
}
