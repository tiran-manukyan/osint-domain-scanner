package com.osint.backend.controller

import com.osint.backend.model.dto.request.ScanRequest
import com.osint.backend.model.dto.response.ScanResponse
import com.osint.backend.model.dto.response.StartScanResponse
import com.osint.backend.service.ScanService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/scans")
class ScanController(
    private val service: ScanService
) {
    @PostMapping
    fun startScan(@RequestBody request: ScanRequest): ResponseEntity<StartScanResponse> =
        ResponseEntity.ok(service.startScan(request))

    @GetMapping
    fun getAll(
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<Page<ScanResponse>> =
        ResponseEntity.ok(service.getAll(pageable))
}
