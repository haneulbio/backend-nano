package com.haneul.nanobackend.encription

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.stereotype.Component

@Component
class TokenEncryptor(
    @Value("\${app.token-encryption.password}") password: String,
    @Value("\${app.token-encryption.salt-hex}") saltHex: String
) {
    private val encryptor: TextEncryptor = Encryptors.text(
        password.ifBlank { error("app.token-encryption.password is blank") },
        saltHex.ifBlank { error("app.token-encryption.salt-hex is blank") },
    )

    fun enc(raw: String): String = encryptor.encrypt(raw)
    fun dec(enc: String): String = encryptor.decrypt(enc)
}
