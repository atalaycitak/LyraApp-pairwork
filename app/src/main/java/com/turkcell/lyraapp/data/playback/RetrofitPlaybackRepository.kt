package com.turkcell.lyraapp.data.playback

import javax.inject.Inject

/**
 * [PlaybackRepository]'nin Retrofit tabanli gercek implementasyonu.
 *
 * API'nin playback/next yanitindaki `type` alanina gore
 * [PlaybackResult.SongPlayback] veya [PlaybackResult.AdPlayback] donusumu yapilir.
 */
class RetrofitPlaybackRepository @Inject constructor(
    private val api: PlaybackApiService,
) : PlaybackRepository {

    override suspend fun getNextPlayback(songId: String): Result<PlaybackResult> = runCatching {
        val response = api.getNextPlayback(PlaybackNextRequestDto(songId = songId))

        if (!response.isSuccessful) {
            val errorMessage = when (response.code()) {
                401 -> "Oturum suresi doldu. Lutfen tekrar giris yapin."
                404 -> "Sarki bulunamadi."
                else -> "Oynatma bilgisi alinamadi: ${response.code()}"
            }
            throw Exception(errorMessage)
        }

        val body = response.body()
            ?: throw Exception("Oynatma yaniti alinamadi.")

        val data = body.data

        when (data.type) {
            "song" -> {
                val song = data.song
                    ?: throw Exception("Yanıtta sarki bilgisi eksik.")
                val stream = data.stream
                    ?: throw Exception("Yanıtta stream bilgisi eksik.")

                PlaybackResult.SongPlayback(
                    song = song,
                    streamUrl = stream.url,
                    streamExpiresAt = stream.expiresAt,
                    streamMimeType = stream.mimeType
                )
            }
            "ad" -> {
                val ad = data.ad
                    ?: throw Exception("Yanıtta reklam bilgisi eksik.")
                val adStream = data.adStream
                    ?: throw Exception("Yanıtta reklam stream bilgisi eksik.")
                val impressionId = data.impressionId
                    ?: throw Exception("Yanıtta impressionId eksik.")
                val song = data.song
                    ?: throw Exception("Yanıtta sarki bilgisi eksik.")
                val stream = data.stream
                    ?: throw Exception("Yanıtta stream bilgisi eksik.")

                PlaybackResult.AdPlayback(
                    ad = ad,
                    adStreamUrl = adStream.url,
                    adStreamMimeType = adStream.mimeType,
                    impressionId = impressionId,
                    song = song,
                    songStreamUrl = stream.url,
                    songStreamExpiresAt = stream.expiresAt,
                    songStreamMimeType = stream.mimeType
                )
            }
            else -> throw Exception("Bilinmeyen playback tipi: ${data.type}")
        }
    }

    override suspend fun markAdComplete(impressionId: String): Result<Boolean> = runCatching {
        val response = api.markAdComplete(AdCompleteRequestDto(impressionId = impressionId))

        if (!response.isSuccessful) {
            val errorMessage = when (response.code()) {
                401 -> "Oturum suresi doldu."
                404 -> "Reklam impression bulunamadi."
                else -> "Reklam bildirimi gonderilemedi: ${response.code()}"
            }
            throw Exception(errorMessage)
        }

        response.body()?.data?.completed ?: true
    }
}
