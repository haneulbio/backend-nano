package com.haneul.nanobackend.config

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
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/", "/health", "/health/**",
                    "/error",
                    "/oauth2/**", "/login/**",
                    "/review", "/review/**",
                    "/privacy",
                    "/data-deletion",
                    "/demo/**"
                ).permitAll()

                it.requestMatchers("/api/**").authenticated()
                it.anyRequest().authenticated()
            }
            .oauth2Login { oauth ->
                oauth.successHandler(oauthSuccessHandler)
            }
            .logout { it.logoutSuccessUrl("/") }

        return http.build()
    }
}
