package com.haneul.nanobackend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "social_auth")
class SocialAuth(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var provider: String = "",

    @Column(nullable = false)
    var providerUserId: String = "",

    @Column(nullable = false, length = 4096)
    var accessTokenEnc: String = "",

    var expiresAt: Instant? = null,

    @Column(nullable = false)
    var scope: String = ""
)
