package com.haneul.nanobackend.search

import com.haneul.nanobackend.demo.DemoDataService
import com.haneul.nanobackend.demo.Influencer
import com.haneul.nanobackend.demo.MediaType
import org.springframework.stereotype.Service
import kotlin.math.abs
import kotlin.math.max

data class MatchResult(
    val username: String,
    val igUserId: String,
    val followersCount: Int,
    val score: Double,
    val reasons: List<String>
)

@Service
class InfluencerSearchService(
    private val demoData: DemoDataService
) {
    fun searchTop10(intent: SearchIntent): List<MatchResult> {
        val all = demoData.list(limit = Int.MAX_VALUE, offset = 0)

        return all.asSequence()
            .mapNotNull { inf -> score(inf, intent) }
            .sortedByDescending { it.score }
            .take(10)
            .toList()
    }

    private fun score(inf: Influencer, intent: SearchIntent): MatchResult? {
        // Hard filters (optional)
        if (intent.minFollowers != null && inf.followersCount < intent.minFollowers) return null
        if (intent.maxFollowers != null && inf.followersCount > intent.maxFollowers) return null

        val posts = inf.posts
        if (posts.isEmpty()) return null

        val hashtags = posts.flatMap { extractHashtags(it.caption) }
        val hashtagSet = hashtags.toSet()

        val wanted = intent.wantedTags.map { normalizeTag(it) }.filter { it.isNotBlank() }
        val excluded = intent.excludedTags.map { normalizeTag(it) }.filter { it.isNotBlank() }

        if (excluded.any { hashtagSet.contains(it) }) return null

        val tagHits = wanted.count { hashtagSet.contains(it) }
        val tagScore = if (wanted.isEmpty()) 0.0 else tagHits.toDouble() / wanted.size.toDouble()

        val wantedTypes = intent.wantedTypes.map { it.uppercase() }.toSet()
        val typeScore = if (wantedTypes.isEmpty()) 0.0 else {
            val hit = posts.count { wantedTypes.contains(it.mediaType.name) }
            hit.toDouble() / posts.size.toDouble()
        }

        val (adPosts, adRatio) = adStats(posts.map { it.caption })
        if (intent.maxAdRatio != null && adRatio > intent.maxAdRatio) return null

        val avgLikes = posts.sumOf { it.likeCount ?: 0 }.toDouble() / posts.size
        val avgComments = posts.sumOf { it.commentsCount ?: 0 }.toDouble() / posts.size
        val engagementRate = (avgLikes + 3.0 * avgComments) / max(1.0, inf.followersCount.toDouble())

        // Simple weighted score (tune later)
        val score =
            0.45 * tagScore +
                    0.25 * typeScore +
                    0.20 * clamp01(engagementRate * 50.0) + // scale demo ER into 0..1-ish
                    0.10 * (1.0 - adRatio)

        val reasons = buildList {
            if (wanted.isNotEmpty()) add("Tag match: $tagHits/${wanted.size}")
            if (wantedTypes.isNotEmpty()) add("Type match: ${(typeScore * 100).toInt()}%")
            add("Engagement proxy: ${"%.3f".format(engagementRate)}")
            add("Ad ratio: ${"%.2f".format(adRatio)} ($adPosts/${posts.size})")
        }

        return MatchResult(
            username = inf.username,
            igUserId = inf.igUserId,
            followersCount = inf.followersCount,
            score = score,
            reasons = reasons
        )
    }

    private fun extractHashtags(caption: String?): List<String> {
        if (caption.isNullOrBlank()) return emptyList()
        val regex = Regex("""#([0-9A-Za-z가-힣_]+)""")
        return regex.findAll(caption)
            .map { "#" + it.groupValues[1] }
            .toList()
    }

    private fun normalizeTag(tag: String): String {
        val t = tag.trim()
        return if (t.startsWith("#")) t else "#$t"
    }

    private fun adStats(captions: List<String?>): Pair<Int, Double> {
        val isAd = captions.map { c ->
            val s = c?.lowercase() ?: ""
            s.contains("#광고") || s.contains("#협찬") || s.contains("#ad") || s.contains("paid partnership")
        }
        val ad = isAd.count { it }
        val total = captions.size.coerceAtLeast(1)
        return ad to (ad.toDouble() / total.toDouble())
    }

    private fun clamp01(x: Double): Double = when {
        x < 0.0 -> 0.0
        x > 1.0 -> 1.0
        else -> x
    }
}
