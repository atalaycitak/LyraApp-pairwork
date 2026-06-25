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
import com.turkcell.lyraapp.data.song.SongRepository
import com.turkcell.lyraapp.data.download.DownloadedSongDao
import com.turkcell.lyraapp.data.download.SongDownloadManager
import com.turkcell.lyraapp.data.home.HomeRepository
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
    val isDownloaded: Boolean = false
)

@Singleton
class AudioPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songRepository: SongRepository,
    private val downloadedSongDao: DownloadedSongDao,
    private val songDownloadManager: SongDownloadManager,
    private val homeRepository: HomeRepository
) : PlayerController {

    private val _playerState = MutableStateFlow(GlobalPlayerState())
    override val playerState: StateFlow<GlobalPlayerState> = _playerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionPollingJob: Job? = null

    private var player: Player? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private var disposed = false
    private var wasPlayingBeforeInterruption = false

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    if (player?.isPlaying == true) startPositionPolling()
                }
                Player.STATE_ENDED -> {
                    cancelPositionPolling()
                    _playerState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
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

        if (_playerState.value.songId == songId) {
            // Şarkı zaten yüklü, çalmıyorsa başlat
            if (player?.isPlaying == false) player?.play()
            return
        }

        scope.launch {
            // MediaController asenkron yüklendiği için hazır olmasını bekle
            while (player == null && !disposed) {
                delay(50)
            }
            if (disposed) return@launch

            _playerState.update { it.copy(isLoading = true, errorMessage = null) }

            cancelPositionPolling()
            player?.stop()
            player?.clearMediaItems()

            val songResult = songRepository.getSongById(songId)
            songResult.onFailure { error ->
                _playerState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Şarkı bilgisi yüklenemedi.") }
                return@launch
            }

            val song = songResult.getOrThrow()
            val (colorStart, colorEnd) = ArtworkPalette.colorPairForId(song.id)

            val downloadedEntity = withContext(Dispatchers.IO) {
                downloadedSongDao.getBySongId(song.id)
            }
            val isDownloaded = downloadedEntity != null

            _playerState.update {
                it.copy(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    artworkStartColor = colorStart,
                    artworkEndColor = colorEnd,
                    durationMs = song.durationMs,
                    currentPositionMs = 0L,
                    isDownloaded = isDownloaded
                )
            }

            if (isDownloaded && downloadedEntity != null) {
                val localFile = File(downloadedEntity.filePath)
                if (localFile.exists()) {
                    _playerState.update { it.copy(isLoading = false) }
                    val mediaItem = MediaItem.Builder()
                        .setUri(android.net.Uri.fromFile(localFile))
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .build()
                        )
                        .build()
                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                    player?.play()
                    
                    // Playback basladiginda backend'e bildir
                    homeRepository.recordPlay(song.id)
                    return@launch
                }
            }

            val urlResult = songRepository.getStreamUrl(songId)
            _playerState.update { it.copy(isLoading = false) }
            urlResult
                .onSuccess { streamData ->
                    val mediaItem = MediaItem.Builder()
                        .setUri(streamData.url)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .build()
                        )
                        .build()
                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                    player?.play()
                    
                    // Playback basladiginda backend'e bildir
                    homeRepository.recordPlay(song.id)
                }
                .onFailure { error ->
                    _playerState.update { it.copy(errorMessage = error.message ?: "Ses akışı başlatılamadı.") }
                }
        }
    }

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
