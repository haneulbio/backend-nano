package com.haneul.nanobackend

class QuoteNotFoundException(
    private val id: Long
): RuntimeException("A quote with ID $id is not found")