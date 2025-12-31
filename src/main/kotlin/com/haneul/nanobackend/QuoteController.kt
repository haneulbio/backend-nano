package com.haneul.nanobackend

import com.haneul.nanobackend.service.QuotesService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

@Component
class Test {
    init {
        println("init")
    }
}

@RestController
@RequestMapping("/quotes")
class QuoteController(
    private val quotesService: QuotesService,
    private val restTemplate: RestTemplate
) {
    val quotes = mutableListOf<QuoteDto>()

    @GetMapping
    fun loadQuotes(@RequestParam("q", required = false) query: String?): List<QuoteDto> {
        return quotesService.loadQuotes(query)
    }

    @PostMapping
    fun postQuote(@Valid @RequestBody quoteDto: QuoteDto): QuoteDto {
        return quotesService.insertQuote(quoteDto)
    }

    @PutMapping
    fun putQuote(@RequestBody quoteDto: QuoteDto): QuoteDto {
        return quotesService.updateQuote(quoteDto)
    }

    @DeleteMapping("/quote/{id}")
    fun deleteQuote(@PathVariable("id") id: Long) {
        quotesService.deleteQuote(id)
    }
}