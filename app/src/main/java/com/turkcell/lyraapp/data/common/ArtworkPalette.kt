package com.turkcell.lyraapp.data.common

import kotlin.math.abs

object ArtworkPalette {
    val ENTRIES = listOf(
        Pair(0xFF8B6FB8L, 0xFF4A3D6BL),
        Pair(0xFF7C83D9L, 0xFF3E4486L),
        Pair(0xFFD98E4AL, 0xFF8A5526L),
        Pair(0xFF4AC2A8L, 0xFF1F6E5CL),
        Pair(0xFF6FBF5AL, 0xFF356B2AL),
        Pair(0xFF5AAFC9L, 0xFF2A5F73L),
        Pair(0xFF9B7FC4L, 0xFF5A4480L),
        Pair(0xFF6B5FB8L, 0xFF3A3270L),
        Pair(0xFF3FAE9CL, 0xFF1E5D52L),
        Pair(0xFFD9604AL, 0xFF8A3020L),
        Pair(0xFF4A8BD9L, 0xFF1E4580L),
        Pair(0xFFD9A84AL, 0xFF8A6020L),
    )

    fun colorPairForId(id: String): Pair<Long, Long> {
        val index = abs(id.hashCode()) % ENTRIES.size
        return ENTRIES[index]
    }
}
