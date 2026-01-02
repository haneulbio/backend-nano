package com.haneul.nanobackend.demo

import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Service
class DemoReportService(
    private val demoData: DemoDataService
) {
    private val iso = DateTimeFormatter.ISO_INSTANT

    fun buildReport(limit: Int, offset: Int, minFollowers: Int?, maxFollowers: Int?): List<InfluencerReportRow> {
        val influencers = demoData
            .list(limit = Int.MAX_VALUE, offset = 0)
            .asSequence()
            .filter { inf ->
                (minFollowers == null || inf.followersCount >= minFollowers) &&
                        (maxFollowers == null || inf.followersCount <= maxFollowers)
            }
            .drop(offset)
            .take(limit)
            .toList()

        return influencers.map { inf ->
            val posts = inf.posts
            val postsAnalyzed = posts.size.coerceAtLeast(1)

            val totalLikes = posts.sumOf { it.likeCount ?: 0 }
            val totalComments = posts.sumOf { it.commentsCount ?: 0 }

            val avgLikes = totalLikes.toDouble() / postsAnalyzed
            val avgComments = totalComments.toDouble() / postsAnalyzed

            val (adPosts, adRatio) = computeAdStats(posts)
            val topHashtags = computeTopHashtags(posts, topK = 12)

            val preview = posts
                .sortedByDescending { it.timestamp }
                .take(5)
                .map { p ->
                    val tags = extractHashtags(p.caption)
                    PostPreview(
                        igMediaId = p.igMediaId,
                        timestamp = iso.format(p.timestamp),
                        mediaType = p.mediaType.name,
                        likeCount = p.likeCount,
                        commentsCount = p.commentsCount,
                        viewCount = p.viewCount,
                        isAd = isAdCaption(p.caption),
                        hashtags = tags.take(10)
                    )
                }

            InfluencerReportRow(
                igUserId = inf.igUserId,
                username = inf.username,
                followersCount = inf.followersCount,
                mediaCount = inf.mediaCount,

                postsAnalyzed = posts.size,
                totalLikes = totalLikes,
                totalComments = totalComments,
                avgLikesPerPost = round2(avgLikes),
                avgCommentsPerPost = round2(avgComments),

                adPosts = adPosts,
                adRatio = round3(adRatio),

                topHashtags = topHashtags,
                recentPostsPreview = preview
            )
        }
    }

    private fun computeAdStats(posts: List<Post>): Pair<Int, Double> {
        if (posts.isEmpty()) return 0 to 0.0
        val ad = posts.count { isAdCaption(it.caption) }
        return ad to (ad.toDouble() / posts.size.toDouble())
    }

    // Simple “self-disclosed ad” markers
    private fun isAdCaption(caption: String?): Boolean {
        if (caption.isNullOrBlank()) return false
        val c = caption.lowercase(Locale.getDefault())
        return listOf(" #광고", "#광고", " #협찬", "#협찬", " paid partnership", "#ad", " #ad").any { c.contains(it) }
    }

    private fun computeTopHashtags(posts: List<Post>, topK: Int): List<HashtagCount> {
        val freq = LinkedHashMap<String, Int>()
        posts.forEach { p ->
            extractHashtags(p.caption).forEach { tag ->
                freq[tag] = (freq[tag] ?: 0) + 1
            }
        }
        return freq.entries
            .sortedByDescending { it.value }
            .take(topK)
            .map { HashtagCount(it.key, it.value) }
    }

    private fun extractHashtags(caption: String?): List<String> {
        if (caption.isNullOrBlank()) return emptyList()
        // Capture hashtags like #두피케어 #haircare #미쟝센
        val regex = Regex("""#([0-9A-Za-z가-힣_]+)""")
        return regex.findAll(caption)
            .map { "#" + it.groupValues[1] }
            .toList()
    }

    private fun round2(x: Double): Double = (x * 100.0).roundToInt() / 100.0
    private fun round3(x: Double): Double = (x * 1000.0).roundToInt() / 1000.0
}
