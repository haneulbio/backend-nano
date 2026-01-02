package com.haneul.nanobackend.demo

data class InfluencerReportRow(
    val igUserId: String,
    val username: String,
    val followersCount: Int,
    val mediaCount: Int,

    val postsAnalyzed: Int,
    val totalLikes: Int,
    val totalComments: Int,
    val avgLikesPerPost: Double,
    val avgCommentsPerPost: Double,

    val adPosts: Int,
    val adRatio: Double,

    val topHashtags: List<HashtagCount>,
    val recentPostsPreview: List<PostPreview>
)

data class HashtagCount(val tag: String, val count: Int)

data class PostPreview(
    val igMediaId: String,
    val timestamp: String,
    val mediaType: String,
    val likeCount: Int?,
    val commentsCount: Int?,
    val viewCount: Int?,
    val isAd: Boolean,
    val hashtags: List<String>
)
