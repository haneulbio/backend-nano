package com.haneul.nanobackend.repository

import com.haneul.nanobackend.entity.SocialAuth
import org.springframework.data.jpa.repository.JpaRepository

interface SocialAuthRepository : JpaRepository<SocialAuth, Long> {
    fun findByProviderAndProviderUserId(provider: String, providerUserId: String): SocialAuth?
}
