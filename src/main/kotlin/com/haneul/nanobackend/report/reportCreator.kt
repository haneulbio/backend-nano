package com.haneul.nanobackend.report

import com.haneul.nanobackend.search.MatchResult
import com.haneul.nanobackend.search.SearchIntent
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.format.DateTimeFormatter

@Service
class PdfReportService(
    private val templateEngine: TemplateEngine
) {
    private val iso = DateTimeFormatter.ISO_INSTANT

    fun buildPdf(
        userPrompt: String,
        intent: SearchIntent,
        results: List<MatchResult>,
        generatedAt: Instant = Instant.now()
    ): ByteArray {
        val ctx = Context().apply {
            setVariable("generatedAt", iso.format(generatedAt))
            setVariable("userPrompt", userPrompt)

            // If your template uses intent.* directly, you can pass the whole object instead.
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

        // Template name WITHOUT ".html"
        val html = templateEngine.process("report", ctx)

        val cleaned = html.removePrefix("\uFEFF").trimStart()


        val out = ByteArrayOutputStream()

        val fontBytes = ClassPathResource("fonts/NotoSansKR-Regular.ttf")
            .inputStream.use { it.readBytes() }

        PdfRendererBuilder()
            .useFastMode()
            .withHtmlContent(cleaned, "http://localhost/")  // baseUri must be a valid URI
            .useFont({ ByteArrayInputStream(fontBytes) }, "NotoSansKR")
            .toStream(out)
            .run()

        return out.toByteArray()
    }
}
