package com.turkcell.lyraapp.data.nowplaying

/**
 * Now Playing ekranı içeriğinin veri kaynağı soyutlaması.
 *
 * Backend REST API'si hazır olmadığından geçici implementasyon [MockNowPlayingRepository]'dir;
 * gerçek API geldiğinde yalnızca implementasyon ve `di/NowPlayingModule.kt` bağlaması değişir
 * (bkz. mvi-overview.md §6).
 */
interface NowPlayingRepository {

    /** Şu anda çalmakta olan parçanın bilgilerini döndürür. */
    suspend fun getNowPlayingInfo(): Result<NowPlayingInfo>

    /** Parçayı favorilere ekler veya favorilerden çıkarır. */
    suspend fun toggleFavorite(trackId: String): Result<Boolean>
}
