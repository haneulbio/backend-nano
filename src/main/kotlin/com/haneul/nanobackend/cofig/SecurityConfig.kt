package com.haneul.nanobackend.config

import com.haneul.nanobackend.auth.OAuthSuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class SecurityConfig(
    private val oauthSuccessHandler: OAuthSuccessHandler
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }

            .cors { it.configurationSource(corsConfigurationSource()) }

            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

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
            .oauth2Login {
                it.successHandler(oauthSuccessHandler)
            }
            .logout {
                it.logoutSuccessUrl("/")
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("https://risefrontend.vercel.app/")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Content-Disposition")
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}


@Configuration
class WebCorsConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("https://risefrontend.vercel.app/")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Content-Disposition")
            .allowCredentials(true)
    }
}
