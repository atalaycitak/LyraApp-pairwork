package com.turkcell.lyraapp.ui.nowplaying

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.song.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

/**
 * Now Playing ekranının ViewModel'i (bkz. mvi-viewmodel-rules.md).
 *
 * [songId] navigasyon argümanından [SavedStateHandle] ile alınır. Yükleme adımları:
 * 1. [SongRepository.getSongById] → şarkı metadatasını State'e yazar.
 * 2. [SongRepository.getStreamUrl] → ExoPlayer'a imzalı URL beslenir ve oynatma başlar.
 *
 * ExoPlayer Context gerektirdiğinden [di/NowPlayingModule.kt] içinde ApplicationContext
 * kullanılarak Hilt tarafından sağlanır; ViewModel doğrudan Context tutmaz.
 *
 * ExoPlayer Singleton Güvenliği: loadTrack() her çağrıldığında player önce durdurulur ve
 * medya temizlenir; bu sayede farklı şarkılar arasında geçişte eski ses karışmaz.
 *
 * Progress Tracking: [Player.Listener.onPlaybackStateChanged] oynatma başladığında
 * [startPositionPolling] tetiklenir; coroutine döngüsü [POSITION_POLL_INTERVAL_MS] aralıkla
 * ExoPlayer'ın gerçek pozisyonunu okur. Simülasyon tamamen kaldırıldı.
 */
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val audioPlayerManager: com.turkcell.lyraapp.data.player.AudioPlayerManager
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle["songId"])

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    private val _effect = Channel<NowPlayingEffect>(Channel.BUFFERED)
    val effect: Flow<NowPlayingEffect> = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            audioPlayerManager.playerState.collect { globalState ->
                _uiState.update {
                    it.copy(
                        trackTitle = globalState.title,
                        artistName = globalState.artist,
                        artworkStartColor = globalState.artworkStartColor,
                        artworkEndColor = globalState.artworkEndColor,
                        isPlaying = globalState.isPlaying,
                        currentPositionMs = globalState.currentPositionMs,
                        durationMs = globalState.durationMs,
                        isLoading = globalState.isLoading
                    )
                }
                
                globalState.errorMessage?.let { error ->
                    _effect.send(NowPlayingEffect.ShowError(error))
                }
            }
        }
        
        // Ekran açıldığında ilgili şarkıyı çalmaya başla
        audioPlayerManager.playSong(songId)
    }

    fun onIntent(intent: NowPlayingIntent) {
        when (intent) {
            is NowPlayingIntent.TogglePlayPause -> audioPlayerManager.togglePlayPause()
            is NowPlayingIntent.ToggleFavorite  -> { /* Favori API'si bu iterasyonda yok */ }
            is NowPlayingIntent.ToggleShuffle   -> _uiState.update { it.copy(isShuffleOn = !it.isShuffleOn) }
            is NowPlayingIntent.ToggleRepeat    -> _uiState.update { it.copy(isRepeatOn = !it.isRepeatOn) }
            is NowPlayingIntent.SkipPrevious    -> audioPlayerManager.seekTo(0L)
            is NowPlayingIntent.SkipNext        -> { /* Kuyruk yönetimi bu iterasyonda yok */ }
            is NowPlayingIntent.SeekTo          -> audioPlayerManager.seekTo(intent.positionMs)
            is NowPlayingIntent.NavigateBack    -> viewModelScope.launch { _effect.send(NowPlayingEffect.NavigateBack) }
            is NowPlayingIntent.Retry           -> audioPlayerManager.playSong(songId)
        }
    }
}

