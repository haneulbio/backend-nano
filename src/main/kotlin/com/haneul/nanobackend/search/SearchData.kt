package com.haneul.nanobackend.search

data class SearchIntent(
    val wantedTags: List<String> = emptyList(),          // e.g. ["두피케어","염색","손상모"]
    val excludedTags: List<String> = emptyList(),        // e.g. ["탈모약"]
    val minFollowers: Int? = null,                       // e.g. 1000
    val maxFollowers: Int? = null,                       // e.g. 10000
    val wantedTypes: List<String> = emptyList(),         // e.g. ["REEL","VIDEO","IMAGE"]
    val language: String? = null,                        // e.g. "ko"
    val brand: String? = null,                           // e.g. "miseenscene"
    val maxAdRatio: Double? = null,                      // e.g. 0.2
    val notes: String? = null                            // optional
)

