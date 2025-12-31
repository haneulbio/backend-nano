package com.haneul.nanobackend.cofig

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "quotes")
data class QuotesConfig(
    val search: SearchConfig,
    val validation: ValidationConfig
) {
    data class SearchConfig(
        val ignoreCase: Boolean = true,
        val minLength: Int = 3,
    )

    data class ValidationConfig(
        val minContentLength: Int = 5,
        val maxContentLength: Int = 500,
        val requireAuthor: Boolean = true,
        val allowedCategories: List<String> = emptyList(),
    )
}