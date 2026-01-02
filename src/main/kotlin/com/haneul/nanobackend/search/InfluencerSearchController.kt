package com.haneul.nanobackend.search

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class SearchRequest(val prompt: String)

@RestController
class InfluencerSearchController(
    private val extractor: OpenAiIntentExtractor,
    private val search: InfluencerSearchService
) {
    @PostMapping("/demo/search")
    fun search(@RequestBody req: SearchRequest): Map<String, Any> {
        val intent = extractor.extract(req.prompt)
        val top10 = search.searchTop10(intent)
        return mapOf(
            "intent" to intent,
            "results" to top10
        )
    }
}
