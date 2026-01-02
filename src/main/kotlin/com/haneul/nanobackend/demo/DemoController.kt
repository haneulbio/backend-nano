package com.haneul.nanobackend.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DemoController(
    private val demoData: DemoDataService
) {
    @GetMapping("/demo/influencers")
    fun influencers(
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ) = demoData.list(limit = limit.coerceIn(1, 200), offset = offset.coerceAtLeast(0))
}
