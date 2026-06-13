package com.turkcell.lyraapp.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.nowplaying.NowPlayingRepository
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

/**
 * Now Playing ekranının ViewModel'i (bkz. mvi-viewmodel-rules.md).
 *
 * Parça bilgisi ekran açılışında bir kez yüklenir. Oynatma durumu (play/pause, ilerleme)
 * bu iterasyonda yerel state ile simüle edilir; gerçek medya oynatıcı entegrasyonu
 * ayrı iterasyonda yapılacaktır.
 */
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val nowPlayingRepository: NowPlayingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    private val _effect = Channel<NowPlayingEffect>(Channel.BUFFERED)
    val effect: Flow<NowPlayingEffect> = _effect.receiveAsFlow()

    private var progressJob: Job? = null

    init {
        loadTrack()
    }

    fun onIntent(intent: NowPlayingIntent) {
        when (intent) {
            is NowPlayingIntent.TogglePlayPause -> togglePlayPause()
            is NowPlayingIntent.ToggleFavorite -> toggleFavorite()
            is NowPlayingIntent.ToggleShuffle -> _uiState.update { it.copy(isShuffleOn = !it.isShuffleOn) }
            is NowPlayingIntent.ToggleRepeat -> _uiState.update { it.copy(isRepeatOn = !it.isRepeatOn) }
            is NowPlayingIntent.SkipPrevious -> _uiState.update { it.copy(currentPositionMs = 0L) }
            is NowPlayingIntent.SkipNext -> _uiState.update { it.copy(currentPositionMs = 0L) }
            is NowPlayingIntent.SeekTo -> _uiState.update { it.copy(currentPositionMs = intent.positionMs) }
            is NowPlayingIntent.NavigateBack -> viewModelScope.launch { _effect.send(NowPlayingEffect.NavigateBack) }
            is NowPlayingIntent.Retry -> loadTrack()
        }
    }

    private fun loadTrack() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = nowPlayingRepository.getNowPlayingInfo()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { info ->
                    _uiState.update {
                        it.copy(
                            trackTitle = info.track.title,
                            artistName = info.track.artist,
                            playlistName = info.track.playlistName,
                            artworkStartColor = info.track.artworkStartColor,
                            artworkEndColor = info.track.artworkEndColor,
                            durationMs = info.track.durationMs,
                            currentPositionMs = 93_000L,
                            isFavorite = info.isFavorite,
                            isPlaying = true,
                        )
                    }
                    startProgressSimulation()
                }
                .onFailure { error ->
                    _effect.send(NowPlayingEffect.ShowError(error.message ?: "Parça yüklenemedi."))
                }
        }
    }

    private fun togglePlayPause() {
        val current = _uiState.value
        val newPlaying = !current.isPlaying
        _uiState.update { it.copy(isPlaying = newPlaying) }
        if (newPlaying) {
            startProgressSimulation()
        } else {
            progressJob?.cancel()
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            val trackId = "track-1"
            val result = nowPlayingRepository.toggleFavorite(trackId)
            result
                .onSuccess { newFavorite ->
                    _uiState.update { it.copy(isFavorite = newFavorite) }
                }
                .onFailure { error ->
                    _effect.send(NowPlayingEffect.ShowError(error.message ?: "Favori güncellenemedi."))
                }
        }
    }

    /**
     * Oynatma ilerlemesini simüle eder. Gerçek medya oynatıcı entegrasyonunda
     * bu mekanizma ExoPlayer/MediaSession listener ile değiştirilir.
     */
    private fun startProgressSimulation() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                delay(PROGRESS_UPDATE_INTERVAL_MS)
                val state = _uiState.value
                if (!state.isPlaying) break
                val newPosition = (state.currentPositionMs + PROGRESS_UPDATE_INTERVAL_MS)
                    .coerceAtMost(state.durationMs)
                _uiState.update { it.copy(currentPositionMs = newPosition) }
                if (newPosition >= state.durationMs) break
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
    }

    private companion object {
        const val PROGRESS_UPDATE_INTERVAL_MS = 1_000L
    }
}
