package com.haneul.nanobackend.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DemoReportController(
    private val reportService: DemoReportService
) {
    @GetMapping("/demo/report")
    fun report(
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(required = false) minFollowers: Int?,
        @RequestParam(required = false) maxFollowers: Int?
    ): Map<String, Any> {
        val safeLimit = limit.coerceIn(1, 200)
        val safeOffset = offset.coerceAtLeast(0)

        val rows = reportService.buildReport(
            limit = safeLimit,
            offset = safeOffset,
            minFollowers = minFollowers,
            maxFollowers = maxFollowers
        )

        return mapOf(
            "mode" to "DEMO",
            "limit" to safeLimit,
            "offset" to safeOffset,
            "filters" to mapOf("minFollowers" to minFollowers, "maxFollowers" to maxFollowers),
            "count" to rows.size,
            "rows" to rows
        )
    }
}
