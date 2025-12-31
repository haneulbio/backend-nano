package com.haneul.nanobackend.auth

import com.haneul.nanobackend.encription.TokenEncryptor
import com.haneul.nanobackend.repository.SocialAuthRepository
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class InstagramSyncController(
    private val repo: SocialAuthRepository,
    private val tokenEncryptor: TokenEncryptor,
    private val graph: MetaGraphClient,
) {

    @GetMapping("/instagram/sync")
    fun sync(auth: OAuth2AuthenticationToken): Map<String, Any> {
        val providerUserId = auth.name
        val row = repo.findByProviderAndProviderUserId("meta", providerUserId)
            ?: error("No saved token. Connect first.")

        val userToken = tokenEncryptor.dec(row.accessTokenEnc)

        val accountsJson = graph.meAccounts(userToken)
        // Next steps:
        // 1) parse JSON -> pick a pageId + pageAccessToken
        // 2) graph.pageInstagramBusinessAccount(pageId, pageAccessToken)
        // 3) parse -> igUserId
        // 4) graph.igProfile(igUserId, userToken or page token depending on endpoint)

        return mapOf("meAccounts" to accountsJson)
    }
}
