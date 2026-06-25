package com.turkcell.lyraapp.data.player

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.common.ArtworkPalette
import com.turkcell.lyraapp.data.song.SongRepository
import com.turkcell.lyraapp.data.download.DownloadedSongDao
import com.turkcell.lyraapp.data.download.SongDownloadManager
import com.turkcell.lyraapp.data.home.HomeRepository
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
    val player: ExoPlayer,
    private val songRepository: SongRepository,
    private val downloadedSongDao: DownloadedSongDao,
    private val songDownloadManager: SongDownloadManager,
    private val homeRepository: HomeRepository
) : PlayerController {
    private val _playerState = MutableStateFlow(GlobalPlayerState())
    override val playerState: StateFlow<GlobalPlayerState> = _playerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionPollingJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    if (player.isPlaying) startPositionPolling()
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
            }
            _playerState.update { it.copy(isPlaying = isPlaying) }
        }
    }

    init {
        player.addListener(playerListener)
    }

    override fun playSong(songId: String) {
        if (_playerState.value.songId == songId) {
            // Şarkı zaten yüklü, çalmıyorsa başlat
            if (!player.isPlaying) player.play()
            return
        }

        scope.launch {
            _playerState.update { it.copy(isLoading = true, errorMessage = null) }

            cancelPositionPolling()
            player.stop()
            player.clearMediaItems()

            val songResult = songRepository.getSongById(songId)
            songResult.onFailure { error ->
                _playerState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Şarkı bilgisi yüklenemedi.") }
                return@launch
            }

            val song = songResult.getOrThrow()
            val (colorStart, colorEnd) = ArtworkPalette.colorPairForId(song.id)

            val downloadedEntity = kotlinx.coroutines.withContext(Dispatchers.IO) {
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
                val localFile = java.io.File(downloadedEntity.filePath)
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
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                    
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
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                    
                    // Playback basladiginda backend'e bildir
                    homeRepository.recordPlay(song.id)
                }
                .onFailure { error ->
                    _playerState.update { it.copy(errorMessage = error.message ?: "Ses akışı başlatılamadı.") }
                }
        }
    }

    override fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    /**
     * Dinleyici ve polling islerini temizler.
     * ExoPlayer'in yasam dongusu PlaybackService tarafindan yonetildigi icin
     * burada player.release() cagirilmaz.
     */
    override fun release() {
        cancelPositionPolling()
        player.removeListener(playerListener)
        player.stop()
        player.clearMediaItems()
        scope.cancel()
    }

    override fun downloadCurrentSong() {
        val currentSongId = _playerState.value.songId ?: return
        scope.launch {
            songDownloadManager.downloadSong(currentSongId)
            _playerState.update { it.copy(isDownloaded = true) }
        }
    }

    override fun removeDownload() {
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
                if (!player.isPlaying) break
                _playerState.update { it.copy(currentPositionMs = player.currentPosition) }
            }
        }
    }

    private companion object {
        private const val POLLING_INTERVAL_MS = 500L
    }

}
