package com.osint.backend.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scan")
class ScanProperties {
    var maxActiveContainers: Int = 10
}