package com.haneul.nanobackend

import com.haneul.nanobackend.cofig.QuotesConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(QuotesConfig::class)
class NanobackendApplication

fun main(args: Array<String>) {
    runApplication<NanobackendApplication>(*args)
}
