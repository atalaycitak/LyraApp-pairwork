package com.turkcell.lyraapp.data.song

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit HTTP interface tanımları.
 *
 * Tüm endpoint'ler openapi.json spesifikasyonuna (paths bölümüne) birebir karşılık gelir.
 * Suspend fonksiyonlar Retrofit'in Kotlin coroutine desteği ile çalışır.
 */
interface SongApiService {

    /**
     * GET /api/v1/songs
     *
     * Şarkı listesini cursor tabanlı sayfalama ile döndürür.
     * [limit] belirtilmezse API varsayılan olarak 20 kullanır.
     * [cursor] ilk sayfa için null bırakılır.
     * [q] arama sorgusu; boş bırakılabilir.
     */
    @GET("api/v1/songs")
    suspend fun getSongs(
        @Query("limit")  limit: Int? = null,
        @Query("cursor") cursor: String? = null,
        @Query("q")      q: String? = null,
    ): SongsResponseDto

    /**
     * GET /api/v1/songs/{id}
     *
     * Belirli bir şarkının detayını döndürür.
     */
    @GET("api/v1/songs/{id}")
    suspend fun getSongById(@Path("id") id: String): SongDetailResponseDto

    /**
     * GET /api/v1/songs/{id}/stream-url
     *
     * Şarkı için 300 saniye geçerli, imzalı bir stream URL'i üretir.
     * ExoPlayer'a beslenmeden hemen önce çağrılmalıdır; önbelleğe alınmamalıdır.
     */
    @GET("api/v1/songs/{id}/stream-url")
    suspend fun getStreamUrl(@Path("id") id: String): StreamUrlResponseDto
}

/**
 * GET /api/v1/songs/{id} yanıt zarfı.
 */
data class SongDetailResponseDto(
    val data: SongDto,
)
