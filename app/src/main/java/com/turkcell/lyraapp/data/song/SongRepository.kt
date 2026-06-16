package com.turkcell.lyraapp.data.song

/**
 * Şarkı kataloğu veri kaynağı soyutlaması.
 *
 * [RetrofitSongRepository] bu interface'i implement eder; DI bağlaması
 * [di/SongModule.kt] üzerinden yapılır. Gerçek implementasyon değiştiğinde
 * yalnızca implementasyon ve DI bağlaması değişir; ViewModel etkilenmez.
 */
interface SongRepository {

    /**
     * İlk [limit] şarkıyı listeler. [cursor] null olduğunda ilk sayfayı getirir.
     * [q] boş veya null olduğunda arama uygulanmaz.
     */
    suspend fun getSongs(
        limit: Int? = null,
        cursor: String? = null,
        q: String? = null,
    ): Result<SongsResponseDto>

    /** Belirli bir şarkının detayını ID ile getirir. */
    suspend fun getSongById(id: String): Result<SongDto>

    /**
     * Şarkı için kısa ömürlü (TTL 300 sn) imzalı stream URL'i üretir.
     * ExoPlayer'a beslenmeden hemen önce çağrılmalıdır.
     */
    suspend fun getStreamUrl(id: String): Result<StreamUrlDataDto>
}
