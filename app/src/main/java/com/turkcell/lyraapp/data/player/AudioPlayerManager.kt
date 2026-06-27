package com.turkcell.lyraapp.data.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.turkcell.lyraapp.data.common.ArtworkPalette
import com.turkcell.lyraapp.data.playback.PlaybackRepository
import com.turkcell.lyraapp.data.playback.PlaybackResult
import com.turkcell.lyraapp.data.profile.ProfileRepository
import com.turkcell.lyraapp.data.song.SongDto
import com.turkcell.lyraapp.data.song.SongRepository
import com.turkcell.lyraapp.data.download.DownloadedSongDao
import com.turkcell.lyraapp.data.download.SongDownloadManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class GlobalPlayerState(
    val songId: String? = null,
    val title: String = "",
    val artist: String = "",
    val artworkStartColor: Long = 0xFF8B6FB8L,
    val artworkEndColor: Long = 0xFF4A3D6BL,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDownloaded: Boolean = false,
    // Reklam alanlari
    val isPlayingAd: Boolean = false,
    val adTitle: String? = null,
    val adAdvertiser: String? = null,
    val adDurationMs: Long = 0L
)

@Singleton
class AudioPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songRepository: SongRepository,
    private val playbackRepository: PlaybackRepository,
    private val profileRepository: ProfileRepository,
    private val downloadedSongDao: DownloadedSongDao,
    private val songDownloadManager: SongDownloadManager,
) : PlayerController {

    private val _playerState = MutableStateFlow(GlobalPlayerState())
    override val playerState: StateFlow<GlobalPlayerState> = _playerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionPollingJob: Job? = null

    private var player: Player? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private var disposed = false
    private var wasPlayingBeforeInterruption = false

    // Reklam sonrasi calinacak sarki bilgisi
    private var pendingSongStreamUrl: String? = null
    private var pendingSongDto: SongDto? = null
    private var pendingImpressionId: String? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    if (player?.isPlaying == true) startPositionPolling()
                }
                Player.STATE_ENDED -> {
                    cancelPositionPolling()
                    // Reklam bitmisse siradaki sarkiyi cal
                    if (_playerState.value.isPlayingAd) {
                        onAdFinished()
                    } else {
                        _playerState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
                    }
                }
                else -> Unit
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startPositionPolling()
            } else {
                cancelPositionPolling()
                if (player?.playbackState == Player.STATE_READY && _playerState.value.songId != null) {
                    wasPlayingBeforeInterruption = true
                }
            }
            _playerState.update { it.copy(isPlaying = isPlaying) }
        }
    }

    private val headsetReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_HEADSET_PLUG &&
                intent.getIntExtra("state", 0) == 1 &&
                wasPlayingBeforeInterruption &&
                player?.isPlaying == false &&
                !disposed
            ) {
                player?.play()
                wasPlayingBeforeInterruption = false
            }
        }
    }

    init {
        context.registerReceiver(
            headsetReceiver,
            IntentFilter(Intent.ACTION_HEADSET_PLUG)
        )
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                if (disposed) return@addListener
                player = controllerFuture?.get()
                player?.addListener(playerListener)
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    override fun playSong(songId: String) {
        if (disposed) return

        if (_playerState.value.songId == songId && !_playerState.value.isPlayingAd) {
            // Sarki zaten yuklu ve reklam calmiyorsa, calmiyorsa baslat
            if (player?.isPlaying == false) player?.play()
            return
        }

        scope.launch {
            // MediaController asenkron yuklendiginden hazir olmasini bekle
            while (player == null && !disposed) {
                delay(50)
            }
            if (disposed) return@launch

            _playerState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    isPlayingAd = false,
                    adTitle = null,
                    adAdvertiser = null,
                    adDurationMs = 0L
                )
            }

            cancelPositionPolling()
            player?.stop()
            player?.clearMediaItems()

            // Playback/next API'sini cagir — bu, dinleme kaydini da otomatik olusturur
            val playbackResult = playbackRepository.getNextPlayback(songId)
            playbackResult.onFailure { error ->
                _playerState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Oynatma bilgisi alinamadi.")
                }
                return@launch
            }

            val result = playbackResult.getOrThrow()

            when (result) {
                is PlaybackResult.SongPlayback -> {
                    playSongDirect(result.song, result.streamUrl)
                }
                is PlaybackResult.AdPlayback -> {
                    playAdThenSong(result)
                }
            }
        }
    }

    override fun playNext() {
        if (disposed) return
        scope.launch {
            val response = songRepository.getSongs(limit = 50)
            response.onSuccess { data ->
                val currentId = _playerState.value.songId
                // Mevcut şarkıdan farklı rastgele bir şarkı seç
                val nextSong = data.data.filter { it.id != currentId }.randomOrNull()
                    ?: data.data.randomOrNull()
                if (nextSong != null) {
                    playSong(nextSong.id)
                }
            }
        }
    }

    /**
     * Premium kullanici veya reklam sirasi gelmemis durumda dogrudan sarkiyi calar.
     */
    private fun playSongDirect(song: SongDto, streamUrl: String) {
        val (colorStart, colorEnd) = ArtworkPalette.colorPairForId(song.id)

        scope.launch {
            val downloadedEntity = withContext(Dispatchers.IO) {
                downloadedSongDao.getBySongId(song.id)
            }
            val isDownloaded = downloadedEntity != null

            // Record the play in the backend so it shows up in "Recently Played"
            scope.launch {
                profileRepository.recordPlay(song.id)
            }

            _playerState.update {
                it.copy(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    artworkStartColor = colorStart,
                    artworkEndColor = colorEnd,
                    durationMs = song.durationMs,
                    currentPositionMs = 0L,
                    isDownloaded = isDownloaded,
                    isLoading = false,
                    isPlayingAd = false,
                    adTitle = null,
                    adAdvertiser = null,
                    adDurationMs = 0L
                )
            }

            // Indirilen sarki varsa yerel dosyadan cal
            if (isDownloaded && downloadedEntity != null) {
                val localFile = File(downloadedEntity.filePath)
                if (localFile.exists()) {
                    val mediaItem = buildMediaItem(
                        uri = android.net.Uri.fromFile(localFile).toString(),
                        title = song.title,
                        artist = song.artist
                    )
                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                    player?.play()
                    return@launch
                }
            }

            // Stream URL ile cal
            val mediaItem = buildMediaItem(
                uri = streamUrl,
                title = song.title,
                artist = song.artist
            )
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
        }
    }

    /**
     * Ucretsiz kullanici — once reklam calar, bittikten sonra sarkiye gecer.
     */
    private fun playAdThenSong(adPlayback: PlaybackResult.AdPlayback) {
        val (colorStart, colorEnd) = ArtworkPalette.colorPairForId(adPlayback.song.id)

        // Sarki bilgilerini sonrasi icin sakla
        pendingSongStreamUrl = adPlayback.songStreamUrl
        pendingSongDto = adPlayback.song
        pendingImpressionId = adPlayback.impressionId

        _playerState.update {
            it.copy(
                songId = adPlayback.song.id,
                title = adPlayback.ad.title,
                artist = adPlayback.ad.advertiser,
                artworkStartColor = colorStart,
                artworkEndColor = colorEnd,
                durationMs = adPlayback.ad.durationMs.toLong(),
                currentPositionMs = 0L,
                isLoading = false,
                isPlayingAd = true,
                adTitle = adPlayback.ad.title,
                adAdvertiser = adPlayback.ad.advertiser,
                adDurationMs = adPlayback.ad.durationMs.toLong()
            )
        }

        val adMediaItem = buildMediaItem(
            uri = adPlayback.adStreamUrl,
            title = adPlayback.ad.title,
            artist = adPlayback.ad.advertiser
        )
        player?.setMediaItem(adMediaItem)
        player?.prepare()
        player?.play()
    }

    /**
     * Reklam bittikten sonra cagrilir:
     * 1. Ad-complete bildirimini gonder
     * 2. Beklettigi sarkiyi cal
     */
    private fun onAdFinished() {
        val impressionId = pendingImpressionId
        val songDto = pendingSongDto
        val streamUrl = pendingSongStreamUrl

        // Pending verileri temizle
        pendingImpressionId = null
        pendingSongDto = null
        pendingSongStreamUrl = null

        if (impressionId == null || songDto == null || streamUrl == null) {
            _playerState.update {
                it.copy(
                    isPlayingAd = false,
                    isPlaying = false,
                    currentPositionMs = 0L,
                    errorMessage = "Reklam sonrasi sarki bilgisi bulunamadi."
                )
            }
            return
        }

        // Ad-complete bildirimini arka planda gonder (fire-and-forget)
        scope.launch {
            playbackRepository.markAdComplete(impressionId)
        }

        // Sarkiyi cal
        playSongDirect(songDto, streamUrl)
    }

    private fun buildMediaItem(uri: String, title: String, artist: String): MediaItem =
        MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .build()
            )
            .build()

    override fun togglePlayPause() {
        if (disposed) return
        if (player?.isPlaying == true) {
            player?.pause()
        } else {
            player?.play()
        }
    }

    override fun seekTo(positionMs: Long) {
        if (disposed) return
        // Reklam calinirken seek engellenir
        if (_playerState.value.isPlayingAd) return
        player?.seekTo(positionMs)
    }

    /**
     * Dinleyici ve polling islerini temizler.
     * ExoPlayer'in yasam dongusu PlaybackService tarafindan yonetildigi icin
     * burada player.release() cagirilmaz, Controller serbest birakilir.
     *
     * Idempotent: birden fazla kez cagrilabilir (PlaybackService.onDestroy +
     * LyraApplication.onTerminate). Ilk cagridan sonraki tum public metotlar
     * disposed kontrolu ile early-return yapar.
     */
    override fun release() {
        if (disposed) return
        disposed = true
        try { context.unregisterReceiver(headsetReceiver) } catch (_: Exception) {}
        cancelPositionPolling()
        player?.removeListener(playerListener)
        player?.stop()
        player?.clearMediaItems()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        player = null
        pendingSongStreamUrl = null
        pendingSongDto = null
        pendingImpressionId = null
        scope.cancel()
        _playerState.update { GlobalPlayerState() }
    }

    override fun downloadCurrentSong() {
        if (disposed) return
        val currentSongId = _playerState.value.songId ?: return
        scope.launch {
            songDownloadManager.downloadSong(currentSongId)
            _playerState.update { it.copy(isDownloaded = true) }
        }
    }

    override fun removeDownload() {
        if (disposed) return
        val currentSongId = _playerState.value.songId ?: return
        scope.launch {
            songDownloadManager.removeDownload(currentSongId)
            _playerState.update { it.copy(isDownloaded = false) }
        }
    }

    private fun cancelPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = null
    }

    private fun startPositionPolling() {
        cancelPositionPolling()
        positionPollingJob = scope.launch {
            while (isActive) {
                delay(POLLING_INTERVAL_MS)
                if (player?.isPlaying != true) break
                _playerState.update { it.copy(currentPositionMs = player?.currentPosition ?: 0L) }
            }
        }
    }

    private companion object {
        private const val POLLING_INTERVAL_MS = 500L
    }

}

