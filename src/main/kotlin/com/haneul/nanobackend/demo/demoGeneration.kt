package com.haneul.nanobackend.demo

import kotlin.math.*
import kotlin.random.Random
import java.time.Instant
import java.time.temporal.ChronoUnit

data class DemoGenConfig(
    val nInfluencers: Int = 2000,

    // follower distribution
    val medianFollowers: Int = 2000,
    val sigma: Double = 0.55,        // smaller => tighter around median
    val clampMin: Int = 1000,
    val clampMax: Int = 10000,

    // optional tail (e.g., 3% become 10k~100k)
    val tailProbability: Double = 0.03,
    val tailMin: Int = 10000,
    val tailMax: Int = 100000,

    // posting behavior
    val postsWindowDays: Int = 90,
    val minPosts: Int = 8,
    val maxPosts: Int = 60,

    // ad marker probability baseline
    val baseAdProb: Double = 0.10
)

class DemoInfluencerGenerator(
    private val cfg: DemoGenConfig,
    seed: Long = 42L
) {
    private val rnd = Random(seed)

    fun generate(): List<Influencer> =
        (1..cfg.nInfluencers).map { idx ->
            val followers = sampleFollowers()
            val username = sampleUsername(idx)
            val mediaCount = sampleMediaCount(followers)
            val posts = samplePosts(idx, followers)
            Influencer(
                igUserId = "demo_ig_${idx.toString().padStart(6, '0')}",
                username = username,
                followersCount = followers,
                mediaCount = mediaCount,
                posts = posts
            )
        }

    private fun sampleFollowers(): Int {
        // lognormal-ish: median = exp(mu)
        val mu = ln(cfg.medianFollowers.toDouble())
        val z = gaussian01()
        val raw = exp(mu + cfg.sigma * z).roundToInt()

        // tail injection
        if (rnd.nextDouble() < cfg.tailProbability) {
            return rnd.nextInt(cfg.tailMin, cfg.tailMax + 1)
        }

        return raw.coerceIn(cfg.clampMin, cfg.clampMax)
    }

    private fun sampleUsername(idx: Int): String {
        val hairTokens = listOf("hair", "scalp", "dye", "perm", "care", "routine", "studio")
        val t1 = hairTokens[rnd.nextInt(hairTokens.size)]
        val t2 = hairTokens[rnd.nextInt(hairTokens.size)]
        val num = (100..999).random(rnd)
        return "demo_${t1}_${t2}_${num}_${idx % 97}"
    }

    private fun sampleMediaCount(followers: Int): Int {
        // loosely correlated: bigger accounts have more posts overall
        val base = when {
            followers < 2000 -> rnd.nextInt(50, 220)
            followers < 5000 -> rnd.nextInt(80, 350)
            followers < 10000 -> rnd.nextInt(120, 500)
            else -> rnd.nextInt(200, 900)
        }
        return base
    }

    private fun samplePosts(idx: Int, followers: Int): List<Post> {
        val nPosts = rnd.nextInt(cfg.minPosts, cfg.maxPosts + 1)
        val now = Instant.now()

        return (1..nPosts).map { p ->
            val mediaType = sampleMediaType()
            val ts = now.minus(rnd.nextInt(0, cfg.postsWindowDays).toLong(), ChronoUnit.DAYS)
                .minus(rnd.nextInt(0, 24).toLong(), ChronoUnit.HOURS)
            val isAd = rnd.nextDouble() < adProbability(followers)

            val caption = sampleCaption(isAd)
            val (likes, comments, views) = sampleEngagement(followers, mediaType)

            Post(
                igMediaId = "demo_media_${idx.toString().padStart(6, '0')}_${p.toString().padStart(3, '0')}",
                timestamp = ts,
                caption = caption,
                mediaType = mediaType,
                likeCount = likes,
                commentsCount = comments,
                viewCount = views
            )
        }
    }

    private fun sampleMediaType(): MediaType {
        val x = rnd.nextDouble()
        return when {
            x < 0.55 -> MediaType.IMAGE
            x < 0.75 -> MediaType.CAROUSEL_ALBUM
            x < 0.90 -> MediaType.REEL
            else -> MediaType.VIDEO
        }
    }

    private fun adProbability(followers: Int): Double {
        // slightly higher ad rate for larger accounts (demo-friendly)
        val bump = when {
            followers < 2000 -> 0.00
            followers < 5000 -> 0.03
            followers < 10000 -> 0.06
            else -> 0.10
        }
        return (cfg.baseAdProb + bump).coerceIn(0.02, 0.40)
    }

    private fun sampleCaption(isAd: Boolean): String {
        val hairHashtags = listOf("#두피케어", "#염색", "#펌", "#헤어루틴", "#손상모", "#헤어에센스", "#샴푸추천")
        val brandHashtags = listOf("#미쟝센", "#려", "#라보에이치", "#아모레퍼시픽")
        val misc = listOf("#일상", "#뷰티", "#셀프케어", "#루틴")

        val tags = mutableListOf<String>()
        tags += hairHashtags.shuffled(rnd).take(rnd.nextInt(2, 5))
        tags += misc.shuffled(rnd).take(rnd.nextInt(1, 3))
        if (rnd.nextDouble() < 0.35) tags += brandHashtags.shuffled(rnd).take(1)

        val adMarker = if (isAd) listOf(" #광고", " #협찬", " #AD").random(rnd) else ""
        val text = listOf(
            "오늘 루틴 공유해요",
            "사용감이 좋아서 추천!",
            "손상모 관리 포인트 정리",
            "두피가 예민할 때 이렇게 해요",
            "염색 후 케어 루틴"
        ).random(rnd)

        return "$text ${tags.joinToString(" ")}$adMarker".trim()
    }

    private fun sampleEngagement(followers: Int, mediaType: MediaType): Triple<Int?, Int?, Int?> {
        // engagement rate decreases as followers rise (realistic)
        val er = when {
            followers < 2000 -> rnd.nextDouble(0.015, 0.045) // 1.5%~4.5%
            followers < 5000 -> rnd.nextDouble(0.010, 0.030)
            followers < 10000 -> rnd.nextDouble(0.007, 0.020)
            else -> rnd.nextDouble(0.004, 0.015)
        }

        // likes base
        val likes = max(0, (followers * er * rnd.nextDouble(0.7, 1.3)).roundToInt())

        // comments roughly 2%~8% of likes
        val comments = max(0, (likes * rnd.nextDouble(0.02, 0.08)).roundToInt())

        // views only for video-ish
        val views = when (mediaType) {
            MediaType.VIDEO, MediaType.REEL -> max(likes, (likes * rnd.nextDouble(5.0, 25.0)).roundToInt())
            else -> null
        }

        return Triple(likes, comments, views)
    }

    // Box–Muller
    private fun gaussian01(): Double {
        val u1 = (rnd.nextDouble().coerceAtLeast(1e-12))
        val u2 = rnd.nextDouble()
        return sqrt(-2.0 * ln(u1)) * cos(2.0 * Math.PI * u2)
    }
}
