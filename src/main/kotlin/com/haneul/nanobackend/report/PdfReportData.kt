package com.haneul.nanobackend.report

import com.haneul.nanobackend.search.SearchIntent
import java.time.Instant

data class PdfReportData(
    val userPrompt: String,
    val intent: SearchIntent,
    val results: List<MatchResult>,
    val generatedAt: Instant
)
