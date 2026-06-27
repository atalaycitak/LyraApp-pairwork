package com.turkcell.lyraapp.data.playback

import com.turkcell.lyraapp.data.song.SongDto

/**
 * Playback domain modelleri.
 *
 * API'nin playback/next yanıtı iki farklı yapıda dönebilir;
 * sealed class ile tip güvenli ayrım sağlanır.
 */
sealed class PlaybackResult {

    /**
     * Doğrudan şarkı oynatımı (premium veya reklam sırası gelmemiş).
     */
    data class SongPlayback(
        val song: SongDto,
        val streamUrl: String,
        val streamExpiresAt: String,
        val streamMimeType: String
    ) : PlaybackResult()

    /**
     * Önce reklam, ardından şarkı oynatımı.
     *
     * İstemci şu sırayı izler:
     * 1. [adStreamUrl] ile reklam sesini çal
     * 2. Reklam bitince [impressionId] ile ad-complete bildir
     * 3. [songStreamUrl] ile gerçek şarkıyı çal
     */
    data class AdPlayback(
        val ad: AdDto,
        val adStreamUrl: String,
        val adStreamMimeType: String,
        val impressionId: String,
        val song: SongDto,
        val songStreamUrl: String,
        val songStreamExpiresAt: String,
        val songStreamMimeType: String
    ) : PlaybackResult()
}
