package com.turkcell.lyraapp.data.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.song.SongRepository
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
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

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
    val errorMessage: String? = null
)

@Singleton
class AudioPlayerManager @Inject constructor(
    val player: ExoPlayer,
    private val songRepository: SongRepository
) {
    private val _playerState = MutableStateFlow(GlobalPlayerState())
    val playerState: StateFlow<GlobalPlayerState> = _playerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionPollingJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    if (player.isPlaying) startPositionPolling()
                }
                Player.STATE_ENDED -> {
                    positionPollingJob?.cancel()
                    _playerState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
                }
                else -> Unit
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startPositionPolling()
            } else {
                positionPollingJob?.cancel()
            }
            _playerState.update { it.copy(isPlaying = isPlaying) }
        }
    }

    init {
        player.addListener(playerListener)
    }

    fun playSong(songId: String) {
        if (_playerState.value.songId == songId) {
            // Şarkı zaten yüklü, çalmıyorsa başlat
            if (!player.isPlaying) player.play()
            return
        }

        scope.launch {
            _playerState.update { it.copy(isLoading = true, errorMessage = null) }

            positionPollingJob?.cancel()
            player.stop()
            player.clearMediaItems()

            val songResult = songRepository.getSongById(songId)
            songResult.onFailure { error ->
                _playerState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Şarkı bilgisi yüklenemedi.") }
                return@launch
            }

            val song = songResult.getOrThrow()
            val (colorStart, colorEnd) = colorPairForId(song.id)

            _playerState.update {
                it.copy(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    artworkStartColor = colorStart,
                    artworkEndColor = colorEnd,
                    durationMs = song.durationMs,
                    currentPositionMs = 0L
                )
            }

            val urlResult = songRepository.getStreamUrl(songId)
            _playerState.update { it.copy(isLoading = false) }
            urlResult
                .onSuccess { streamData ->
                    val mediaItem = MediaItem.fromUri(streamData.url)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                }
                .onFailure { error ->
                    _playerState.update { it.copy(errorMessage = error.message ?: "Ses akışı başlatılamadı.") }
                }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun release() {
        positionPollingJob?.cancel()
        positionPollingJob = null
        player.removeListener(playerListener)
        scope.cancel()
    }

    private fun startPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = scope.launch {
            while (true) {
                delay(500L)
                if (!player.isPlaying) break
                _playerState.update { it.copy(currentPositionMs = player.currentPosition) }
            }
        }
    }

    private fun colorPairForId(id: String): Pair<Long, Long> {
        val index = abs(id.hashCode()) % ARTWORK_PALETTE.size
        return ARTWORK_PALETTE[index]
    }

    private companion object {
        val ARTWORK_PALETTE = listOf(
            Pair(0xFF8B6FB8L, 0xFF4A3D6BL),
            Pair(0xFF7C83D9L, 0xFF3E4486L),
            Pair(0xFFD98E4AL, 0xFF8A5526L),
            Pair(0xFF4AC2A8L, 0xFF1F6E5CL),
            Pair(0xFF6FBF5AL, 0xFF356B2AL),
            Pair(0xFF5AAFC9L, 0xFF2A5F73L),
            Pair(0xFF9B7FC4L, 0xFF5A4480L),
            Pair(0xFF6B5FB8L, 0xFF3A3270L),
            Pair(0xFF3FAE9CL, 0xFF1E5D52L),
            Pair(0xFFD9604AL, 0xFF8A3020L),
            Pair(0xFF4A8BD9L, 0xFF1E4580L),
            Pair(0xFFD9A84AL, 0xFF8A6020L),
        )
    }
}
