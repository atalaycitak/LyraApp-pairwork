package com.turkcell.lyraapp.data.playback

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Playback (oynatma akisi) API endpoint tanimlari.
 *
 * OpenAPI spesifikasyonundaki /api/v1/me/playback/ altindaki uclari karsilar.
 */
interface PlaybackApiService {

    /**
     * POST /api/v1/me/playback/next
     *
     * Bir sarkiyi calmadan hemen once cagrilir. Sunucu dinlemeyi kaydeder ve
     * ne calinacagina karar verir:
     * - Premium kullanicilar daima type: "song" alir.
     * - Ucretsiz kullanicilar her 3 sarkida bir type: "ad" alir.
     *
     * Bu endpoint kullanildiginda ayrica POST /api/v1/me/plays cagirilmamalidir.
     */
    @POST("api/v1/me/playback/next")
    suspend fun getNextPlayback(@Body request: PlaybackNextRequestDto): Response<PlaybackNextResponseDto>

    /**
     * POST /api/v1/me/playback/ad-complete
     *
     * Reklam bittiginde sunucuya bildirim gonderir (analytics).
     * Onceki playback/next yanıtindaki impressionId ile cagirilir.
     */
    @POST("api/v1/me/playback/ad-complete")
    suspend fun markAdComplete(@Body request: AdCompleteRequestDto): Response<AdCompleteResponseDto>
}
