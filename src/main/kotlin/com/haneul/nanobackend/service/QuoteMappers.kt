package com.haneul.nanobackend.service

import com.haneul.nanobackend.QuoteDto
import com.haneul.nanobackend.QuoteEntity

fun QuoteEntity.toDto(): QuoteDto {
    return QuoteDto(
        id = this.id,
        content = this.content,
        author = this.author,
    )
}

fun QuoteDto.toEntity(): QuoteEntity {
    return QuoteEntity(
        id = this.id,
        content = this.content,
        author = this.author,
    )
}