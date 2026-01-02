package com.haneul.nanobackend.report

import com.haneul.nanobackend.search.InfluencerSearchService
import com.haneul.nanobackend.search.OpenAiIntentExtractor
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.Instant

@RestController
class ReportDebugController(
    private val extractor: OpenAiIntentExtractor,
    private val search: InfluencerSearchService,
    private val templateEngine: TemplateEngine
) {

    @GetMapping("/demo/search/report/html")
    fun debugHtml(@RequestParam prompt: String): String {
        val intent = extractor.extract(prompt)
        val results = search.searchTop10(intent)

        val ctx = Context().apply {
            setVariable("generatedAt", Instant.now().toString())
            setVariable("userPrompt", prompt)

            setVariable("intentMinFollowers", intent.minFollowers ?: "N/A")
            setVariable("intentMaxFollowers", intent.maxFollowers ?: "N/A")
            setVariable("intentWantedTags", intent.wantedTags.joinToString(", ").ifBlank { "N/A" })
            setVariable("intentWantedTypes", intent.wantedTypes.joinToString(", ").ifBlank { "N/A" })
            setVariable("intentMaxAdRatio", intent.maxAdRatio ?: "N/A")

            setVariable("results", results.map {
                mapOf(
                    "username" to it.username,
                    "followersCount" to it.followersCount,
                    "score" to String.format("%.3f", it.score),
                    "reasons" to it.reasons
                )
            })
        }

        // IMPORTANT: template name WITHOUT ".html"
        return templateEngine.process("report", ctx)
    }
}