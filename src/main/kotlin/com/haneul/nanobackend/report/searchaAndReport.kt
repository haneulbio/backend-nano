package com.haneul.nanobackend.report

import com.haneul.nanobackend.search.InfluencerSearchService
import com.haneul.nanobackend.search.OpenAiIntentExtractor
import com.haneul.nanobackend.search.SearchRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DemoSearchReportController(
    private val extractor: OpenAiIntentExtractor,
    private val search: InfluencerSearchService,
    private val pdf: PdfReportService
) {

    @PostMapping("/demo/search/report")
    fun searchAndReport(@RequestBody req: SearchRequest): ResponseEntity<ByteArray> {
        println("RAW BODY = $req")
        val intent = extractor.extract(req.prompt)
        val results = search.searchTop10(intent)

        val bytes = pdf.buildPdf(
            userPrompt = req.prompt,
            intent = intent,
            results = results
        )

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rise_demo_report.pdf")
            .body(bytes)
    }

}
