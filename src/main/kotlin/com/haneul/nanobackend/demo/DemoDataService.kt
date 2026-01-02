package com.haneul.nanobackend.demo

import org.springframework.stereotype.Service

@Service
class DemoDataService {
    private val generator = DemoInfluencerGenerator(DemoGenConfig(nInfluencers = 2000), seed = 123L)
    private val data: List<Influencer> by lazy { generator.generate() }

    fun list(limit: Int = 50, offset: Int = 0): List<Influencer> =
        data.drop(offset).take(limit)

    fun getByUsername(username: String): Influencer? =
        data.firstOrNull { it.username == username }
}
