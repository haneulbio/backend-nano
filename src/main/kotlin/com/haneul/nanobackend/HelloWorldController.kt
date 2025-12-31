package com.haneul.nanobackend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController {

    @GetMapping("/helloWorld")
    fun helloWorld(): String = "Hello World"
}