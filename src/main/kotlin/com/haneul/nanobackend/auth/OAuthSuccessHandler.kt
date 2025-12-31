package com.haneul.nanobackend.auth

import com.haneul.nanobackend.encription.TokenEncryptor
import com.haneul.nanobackend.entity.SocialAuth
import com.haneul.nanobackend.repository.SocialAuthRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuthSuccessHandler(
    private val clientService: OAuth2AuthorizedClientService,
    private val repo: SocialAuthRepository,
    private val tokenEncryptor: TokenEncryptor,
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauth = authentication as OAuth2AuthenticationToken
        val regId = oauth.authorizedClientRegistrationId
        val principalName = oauth.name

        val client: OAuth2AuthorizedClient =
            clientService.loadAuthorizedClient(regId, principalName)
                ?: error("No authorized client found for regId=$regId principal=$principalName")

        val accessToken = client.accessToken.tokenValue
        val expiresAt = client.accessToken.expiresAt
        val scopes = client.accessToken.scopes.joinToString(" ")

        val providerUserId = oauth.name

        val row = repo.findByProviderAndProviderUserId("meta", providerUserId)
            ?: SocialAuth(
                provider = "meta",
                providerUserId = providerUserId,
                accessTokenEnc = "",
                scope = ""
            )

        row.accessTokenEnc = tokenEncryptor.enc(accessToken)
        row.expiresAt = expiresAt
        row.scope = scopes
        repo.save(row)

        response.sendRedirect("/connected")
    }
}
