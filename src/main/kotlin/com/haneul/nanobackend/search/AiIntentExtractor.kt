package com.haneul.nanobackend.search

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class OpenAiIntentExtractor(
    @Value("\${openai.api-key}") private val apiKey: String,
    private val om: ObjectMapper
) {
    private val rest = RestClient.builder()
        .baseUrl("https://api.openai.com/v1")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKey") // Bearer auth :contentReference[oaicite:3]{index=3}
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    fun extract(prompt: String): SearchIntent {
        val body: Map<String, Any> = mapOf(
            "model" to "gpt-5.2",
            "input" to listOf(
                mapOf("role" to "system", "content" to
                        "Extract influencer search intent. Return ONLY JSON matching the schema."
                ),
                mapOf("role" to "user", "content" to prompt)
            ),
            "text" to mapOf(
                "format" to mapOf(
                    "type" to "json_schema",
                    "name" to "search_intent",
                    "schema" to mapOf(
                        "type" to "object",
                        "additionalProperties" to false,
                        "properties" to mapOf(
                            "wantedTags" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                            "excludedTags" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                            "minFollowers" to mapOf("type" to listOf("integer", "null")),
                            "maxFollowers" to mapOf("type" to listOf("integer", "null")),
                            "wantedTypes" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                            "language" to mapOf("type" to listOf("string", "null")),
                            "brand" to mapOf("type" to listOf("string", "null")),
                            "maxAdRatio" to mapOf("type" to listOf("number", "null")),
                            "notes" to mapOf("type" to listOf("string", "null"))
                        ),
                        "required" to listOf(
                            "wantedTags","excludedTags","minFollowers","maxFollowers",
                            "wantedTypes","language","brand","maxAdRatio","notes"
                        )
                    ),
                    "strict" to true
                )
            )
        )


        val raw = rest.post()
            .uri("/responses")
            .body(body)
            .retrieve()
            .body(String::class.java) ?: error("Empty OpenAI response")

        // Responses API returns content in an output array; easiest robust approach is parse and find first JSON text chunk
        val tree = om.readTree(raw)
        val jsonText = tree
            .path("output").firstOrNull()
            ?.path("content")?.firstOrNull()
            ?.path("text")?.asText()
            ?: error("Could not locate structured output text")

        return om.readValue(jsonText, SearchIntent::class.java)
    }

    private fun JsonNode.firstOrNull(): JsonNode? =
        if (this.isArray && this.size() > 0) this[0] else null

    companion object {
        private val searchIntentJsonSchema: Map<String, Any> = mapOf(
            "name" to "search_intent",
            "schema" to mapOf(
                "type" to "object",
                "additionalProperties" to false,
                "properties" to mapOf(
                    "wantedTags" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                    "excludedTags" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                    "minFollowers" to mapOf("type" to listOf("integer","null")),
                    "maxFollowers" to mapOf("type" to listOf("integer","null")),
                    "wantedTypes" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                    "language" to mapOf("type" to listOf("string","null")),
                    "brand" to mapOf("type" to listOf("string","null")),
                    "maxAdRatio" to mapOf("type" to listOf("number","null")),
                    "notes" to mapOf("type" to listOf("string","null"))
                ),
                "required" to listOf("wantedTags","excludedTags","minFollowers","maxFollowers","wantedTypes","language","brand","maxAdRatio","notes")
            )
        )
    }
}
