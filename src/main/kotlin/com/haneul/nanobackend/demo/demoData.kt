package com.haneul.nanobackend.demo

import java.time.Instant

data class Influencer(
    val igUserId: String,
    val username: String,
    val followersCount: Int,
    val mediaCount: Int,
    val posts: List<Post>
)

data class Post(
    val igMediaId: String,
    val timestamp: Instant,
    val caption: String?,
    val mediaType: MediaType,
    val likeCount: Int?,
    val commentsCount: Int?,
    val viewCount: Int? // 영상 이나 REEL 만
)

enum class MediaType { IMAGE, VIDEO, CAROUSEL_ALBUM, REEL }
