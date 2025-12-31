package com.haneul.nanobackend.auth

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class MetaGraphClient {
    private val rest = RestClient.builder()
        .baseUrl("https://graph.facebook.com/v24.0")
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build()

    fun meAccounts(userAccessToken: String): String =
        rest.get()
            .uri { b ->
                b.path("/me/accounts")
                    .queryParam("fields", "id,name,access_token")
                    .queryParam("access_token", userAccessToken)
                    .build()
            }
            .retrieve()
            .body(String::class.java)!!


    fun pageInstagramBusinessAccount(pageId: String, pageAccessToken: String): String =
        rest.get()
            .uri { b -> b.path("/$pageId")
                .queryParam("fields", "instagram_business_account")
                .queryParam("access_token", pageAccessToken)
                .build()
            }
            .retrieve()
            .body(String::class.java)!!

    fun igProfile(igUserId: String, pageAccessToken: String): String =
        rest.get()
            .uri { b -> b.path("/$igUserId")
                .queryParam("fields", "id,username,media_count")
                .queryParam("access_token", pageAccessToken)
                .build()
            }
            .retrieve()
            .body(String::class.java)!!
}
