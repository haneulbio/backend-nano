package com.haneul.nanobackend.cofig

import com.haneul.nanobackend.auth.OAuthSuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig(
    private val oauthSuccessHandler: OAuthSuccessHandler
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // enable properly if you use browser sessions/forms
            .authorizeHttpRequests {
                it.requestMatchers("/", "/health", "/oauth2/**", "/login/**").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2Login { oauth ->
                oauth.successHandler(oauthSuccessHandler)
            }
            .logout { it.logoutSuccessUrl("/") }

        return http.build()
    }
}
