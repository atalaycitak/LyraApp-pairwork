package com.turkcell.lyraapp.data.home

import com.turkcell.lyraapp.data.song.SongsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApiService {

    /**
     * GET /api/v1/me/recently-played
     * Kullanıcının son dinlediği şarkıları getirir.
     */
    @GET("api/v1/me/recently-played")
    suspend fun getRecentlyPlayed(
        @Query("limit") limit: Int? = null,
    ): SongsResponseDto

    /**
     * GET /api/v1/me/for-you
     * Kullanıcıya özel şarkı listesi ("Senin İçin Müzikler").
     */
    @GET("api/v1/me/for-you")
    suspend fun getForYou(): SongsResponseDto

    /**
     * GET /api/v1/me/recommendations
     * Kullanıcının dinleme geçmişine göre öneriler ("Ne dinlemek istersin?").
     */
    @GET("api/v1/me/recommendations")
    suspend fun getRecommendations(): SongsResponseDto
}
