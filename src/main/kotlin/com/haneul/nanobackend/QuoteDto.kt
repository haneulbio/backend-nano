package com.haneul.nanobackend

import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length

data class QuoteDto (
    val id: Long,
    @field:Length(
        min = 5,
        max = 500,
        message = "content should be between 5 and 500"
    )
    val content: String,
    @field:Pattern(
        regexp = "\\b[a-zA-Z]+\\b(?:\\s+\\b[a-zA-Z]+\\b)+",
        message = "author must have a first and last name"
    )
    val author: String
)
