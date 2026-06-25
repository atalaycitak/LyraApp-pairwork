package com.turkcell.lyraapp.data.player

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Arka plan muzik oynatma servisi.
 *
 * MediaSessionService, Android sistemine foreground service olarak calisan bir medya
 * oynatici oldugunu bildirir. Bu sayede:
 * - Uygulama arka plana alindigi zaman muzik calmayi durdurmaz.
 * - Sistem bildirim panelinde otomatik medya kontrolleri (oynat/duraklat) gosterilir.
 * - Kulaklik cikarildiginda muzik otomatik durdurulur (handleAudioBecomingNoisy).
 *
 * ExoPlayer singleton olarak Hilt uzerinden enjekte edilir; ayni instance
 * AudioPlayerManager tarafindan da kullanilir.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val currentPlayer = mediaSession?.player
        if (currentPlayer == null || !currentPlayer.playWhenReady ||
            currentPlayer.mediaItemCount == 0 ||
            currentPlayer.playbackState == Player.STATE_ENDED
        ) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
