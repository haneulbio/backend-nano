package com.haneul.nanobackend.repository

import com.haneul.nanobackend.QuoteDto
import com.haneul.nanobackend.QuoteEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


interface QuotesRepository: JpaRepository<QuoteEntity, Long> {
    fun findByContentContainsIgnoreCase(query: String): List<QuoteEntity>


}