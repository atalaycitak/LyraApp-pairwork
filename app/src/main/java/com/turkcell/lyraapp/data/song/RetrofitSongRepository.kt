package com.turkcell.lyraapp.data.song

import javax.inject.Inject

/**
 * [SongRepository]'nin Retrofit tabanlı gerçek implementasyonu.
 *
 * Her metot ağ hatalarını [runCatching] ile yakalayıp [Result.failure] olarak döndürür;
 * çağıran katman (ViewModel veya diğer repository'ler) başarı/hata dallarını işler.
 */
class RetrofitSongRepository @Inject constructor(
    private val api: SongApiService,
) : SongRepository {

    override suspend fun getSongs(
        limit: Int?,
        cursor: String?,
        q: String?,
    ): Result<SongsResponseDto> = runCatching {
        api.getSongs(limit = limit, cursor = cursor, q = q)
    }

    override suspend fun getSongById(id: String): Result<SongDto> = runCatching {
        api.getSongById(id).data
    }

    override suspend fun getStreamUrl(id: String): Result<StreamUrlDataDto> = runCatching {
        try {
            api.getStreamUrl(id).data
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 403) {
                throw Exception("Bu işlemi gerçekleştirebilmek için Premium üye olmalısınız.")
            }
            throw e
        }
    }
}
