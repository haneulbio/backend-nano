package com.haneul.nanobackend.review

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.haneul.nanobackend.auth.MetaGraphClient
import com.haneul.nanobackend.encription.TokenEncryptor
import com.haneul.nanobackend.repository.SocialAuthRepository
import org.springframework.http.MediaType
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

@RestController
class MetaNeedController(){
    @GetMapping("/privacy", produces = [MediaType.TEXT_HTML_VALUE])
    fun privacy(): String = """
        <h1>Privacy Policy</h1>
        <p>This application ("nano2") collects Facebook and Instagram account data only with explicit user consent.</p>
        
        <h2>Collected Data</h2>
        <ul>
          <li>Facebook user ID</li>
          <li>Encrypted access tokens</li>
          <li>Facebook Pages and connected Instagram Business account information</li>
        </ul>
        
        <h2>Purpose</h2>
        <p>Data is used solely for influencer analysis and collaboration eligibility evaluation.</p>
        
        <h2>Data Storage</h2>
        <p>All access tokens are encrypted at rest. Data is never sold or shared.</p>
        
        <h2>Data Deletion</h2>
        <p>Users may request deletion at any time via the Data Deletion Request endpoint.</p>
        
        <h2>Contact</h2>
        <p>Email: support@nano2.example</p>
        """
    @GetMapping("/data-deletion", produces = [MediaType.TEXT_HTML_VALUE])
    fun dataDeletion(): String = """
        <h1>Data Deletion Request</h1>
        <p>If you wish to delete your data associated with nano2, please contact:</p>
        <p><strong>Email:</strong> support@nano2.example</p>
        <p>Your request will be processed within 7 days.</p>
        """

}