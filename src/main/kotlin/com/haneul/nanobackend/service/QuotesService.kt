package com.haneul.nanobackend.service

import com.haneul.nanobackend.QuoteDto
import com.haneul.nanobackend.QuoteNotFoundException
import com.haneul.nanobackend.cofig.QuotesConfig
import com.haneul.nanobackend.repository.QuotesRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class QuotesService(
    private val quotesRepository: QuotesRepository,
    @param:Value("\${spring.application.version}")
    private val version: String,
    private val quotesConfig: QuotesConfig
) {

    init {
        println("service running with $version")

        println(quotesConfig)
    }

    fun loadQuotes(query: String?): List<QuoteDto>{
        return if(query != null) {
            quotesRepository
                .findByContentContainsIgnoreCase(query)
                .map { it.toDto() }
        } else {
            quotesRepository
                .findAll()
                .map { it.toDto() }
        }
    }

    fun insertQuote(quote: QuoteDto): QuoteDto {
        return quotesRepository
            .save(quote.toEntity()).apply {
                this.id = 0
            }
            .toDto()
    }

    fun updateQuote(quote: QuoteDto): QuoteDto {
        return quotesRepository
            .save(
                quote.toEntity()
            )
            .toDto()
    }

    fun deleteQuote(quoteId: Long){
        quotesRepository.deleteById(quoteId)
    }

}