package com.osint.backend.util

import java.util.*

object ScanNamingUtils {

    fun getContainerName(scanId: UUID, domain: String, prefix: String = "osint-scan"): String =
        "${prefix}-$domain-$scanId"
}
