package com.turkcell.lyraapp.data.search

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [SearchRepository]'nin MOCK (statik veri) implementasyonu.
 *
 * Gerçek bir ağ çağrısı yapmaz; tasarım ekran görüntüsündeki içeriği statik olarak döndürür
 * ve `delay(...)` ile ağ davranışını taklit eder. Gerçek API geldiğinde bu sınıf ağ tabanlı
 * bir implementasyonla değiştirilir; ViewModel ve Contract etkilenmez.
 */
class MockSearchRepository @Inject constructor() : SearchRepository {

    override suspend fun getSearchFeed(query: String?): Result<SearchFeed> {
        delay(NETWORK_DELAY_MS)
        return Result.success(
            SearchFeed(
                filters = FILTERS,
                genres = GENRES,
            ),
        )
    }

    private companion object {
        const val NETWORK_DELAY_MS = 600L

        val FILTERS = listOf(
            SearchFilter("filter-all", "Hepsi"),
            SearchFilter("filter-pop", "Pop"),
            SearchFilter("filter-electronic", "Elektronik"),
            SearchFilter("filter-acoustic", "Akustik"),
        )

        val GENRES = listOf(
            Genre("genre-pop", "Pop", 0xFF3DC5B0, 0xFF1A8A7A),
            Genre("genre-electronic", "Elektronik", 0xFF9B8EC4, 0xFF6B5FB8),
            Genre("genre-acoustic", "Akustik", 0xFFC488C0, 0xFF8B5A87),
            Genre("genre-lofi", "Lo-fi", 0xFF3A7B7B, 0xFF1E4D4D),
            Genre("genre-indie", "Indie", 0xFF6B5F99, 0xFF3E3670),
            Genre("genre-jazz", "Jazz", 0xFF4CAF50, 0xFF2E7D32),
            Genre("genre-classical", "Klasik", 0xFFC97BA0, 0xFF8B4570),
            Genre("genre-travel", "Yolculuk", 0xFFE8907A, 0xFFC06050),
        )
    }
}
