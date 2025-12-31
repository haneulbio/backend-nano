package com.haneul.nanobackend.review

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.haneul.nanobackend.auth.MetaGraphClient
import com.haneul.nanobackend.encription.TokenEncryptor
import com.haneul.nanobackend.repository.SocialAuthRepository
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/meta")
class ReviewMetaApiController(
    private val repo: SocialAuthRepository,
    private val tokenEncryptor: TokenEncryptor,
    private val graph: MetaGraphClient,
    private val om: ObjectMapper
) {

    @GetMapping("/pages")
    fun pages(auth: OAuth2AuthenticationToken): JsonNode {
        val providerUserId = auth.name

        val row = repo.findByProviderAndProviderUserId("meta", providerUserId)
            ?: error("No saved token. Connect first.")

        val userToken = tokenEncryptor.dec(row.accessTokenEnc)

        val accountsJson = graph.meAccounts(userToken)
        return om.readTree(accountsJson)
    }

    @GetMapping("/instagram")
    fun instagram(auth: OAuth2AuthenticationToken): Map<String, Any?> {
        val providerUserId = auth.name

        val row = repo.findByProviderAndProviderUserId("meta", providerUserId)
            ?: error("No saved token. Connect first.")

        val userToken = tokenEncryptor.dec(row.accessTokenEnc)

        // 1) /me/accounts
        val accountsTree = om.readTree(graph.meAccounts(userToken))
        val firstPage = accountsTree.path("data").firstOrNull()
            ?: return mapOf(
                "status" to "NO_PAGES",
                "message" to "This user has no accessible Pages. pages_show_list required and user must be Page admin.",
                "meAccounts" to accountsTree
            )

        val pageId = firstPage.path("id").asText(null)
        val pageName = firstPage.path("name").asText(null)
        val pageAccessToken = firstPage.path("access_token").asText(null)

        if (pageId.isNullOrBlank() || pageAccessToken.isNullOrBlank()) {
            return mapOf(
                "status" to "PAGE_TOKEN_MISSING",
                "message" to "Missing page id or page access token in /me/accounts response.",
                "meAccounts" to accountsTree
            )
        }

        // 2) /{pageId}?fields=instagram_business_account
        val pageTree = om.readTree(graph.pageInstagramBusinessAccount(pageId, pageAccessToken))
        val igUserId = pageTree.path("instagram_business_account").path("id").asText(null)

        if (igUserId.isNullOrBlank()) {
            return mapOf(
                "status" to "NO_INSTAGRAM_BUSINESS_ACCOUNT",
                "message" to "The selected Page is not connected to an Instagram Business/Creator account.",
                "selectedPage" to mapOf("id" to pageId, "name" to pageName),
                "pageLookup" to pageTree
            )
        }

        // 3) /{igUserId}?fields=id,username,media_count
        val igProfileTree = om.readTree(graph.igProfile(igUserId, pageAccessToken))

        return mapOf(
            "status" to "OK",
            "selectedPage" to mapOf("id" to pageId, "name" to pageName),
            "igUserId" to igUserId,
            "igProfile" to igProfileTree
        )
    }

    // helper for JsonNode iteration
    private fun JsonNode.firstOrNull(): JsonNode? =
        if (this.isArray && this.size() > 0) this[0] else null
}
