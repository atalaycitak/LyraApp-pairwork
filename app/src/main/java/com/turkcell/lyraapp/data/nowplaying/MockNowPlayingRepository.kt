package com.turkcell.lyraapp.data.nowplaying

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [NowPlayingRepository]'nin MOCK (statik veri) implementasyonu.
 *
 * Gerçek bir ağ çağrısı yapmaz; tasarım ekran görüntüsündeki içeriği statik olarak döndürür
 * ve `delay(...)` ile ağ davranışını taklit eder. Gerçek API geldiğinde bu sınıf ağ tabanlı
 * bir implementasyonla değiştirilir; ViewModel ve Contract etkilenmez.
 */
class MockNowPlayingRepository @Inject constructor() : NowPlayingRepository {

    private var isFavorite = true

    override suspend fun getNowPlayingInfo(): Result<NowPlayingInfo> {
        delay(NETWORK_DELAY_MS)
        return Result.success(
            NowPlayingInfo(
                track = MOCK_TRACK,
                isFavorite = isFavorite,
            ),
        )
    }

    override suspend fun toggleFavorite(trackId: String): Result<Boolean> {
        delay(TOGGLE_DELAY_MS)
        isFavorite = !isFavorite
        return Result.success(isFavorite)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 500L
        const val TOGGLE_DELAY_MS = 200L

        val MOCK_TRACK = Track(
            id = "track-1",
            title = "Neon Sokaklar",
            artist = "Sehir Isiklari",
            playlistName = "Gece Vardiyasi",
            durationMs = 223_000L,
            artworkStartColor = 0xFFD98E4A,
            artworkEndColor = 0xFF8A5526,
        )
    }
}
