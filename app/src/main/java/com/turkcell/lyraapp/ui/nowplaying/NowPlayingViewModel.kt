package com.turkcell.lyraapp.ui.nowplaying

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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
 * [songId] navigasyon argumanindan [SavedStateHandle] ile alinir.
 * Sarki metadatasi ve oynatma [PlayerController] uzerinden saglanir;
 * ViewModel dogrudan ExoPlayer veya Context tutmaz.
 *
 * Karistirma/tekrarlama durumlari is mantigi icermedigi icin
 * ekran seviyesinde yerel state ile yonetilir (bkz. NowPlayingScreen).
 */
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playerController: PlayerController
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle["songId"])

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    private val _effect = Channel<NowPlayingEffect>(Channel.BUFFERED)
    val effect: Flow<NowPlayingEffect> = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            playerController.playerState.collect { globalState ->
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
        
        playerController.playSong(songId)
    }

    fun onIntent(intent: NowPlayingIntent) {
        when (intent) {
            is NowPlayingIntent.TogglePlayPause -> playerController.togglePlayPause()
            is NowPlayingIntent.ToggleFavorite  -> { /* Favori API'si bu iterasyonda yok */ }
            is NowPlayingIntent.SkipPrevious    -> playerController.seekTo(0L)
            is NowPlayingIntent.SkipNext        -> { /* Kuyruk yonetimi bu iterasyonda yok */ }
            is NowPlayingIntent.SeekTo          -> playerController.seekTo(intent.positionMs)
            is NowPlayingIntent.NavigateBack    -> viewModelScope.launch { _effect.send(NowPlayingEffect.NavigateBack) }
            is NowPlayingIntent.Retry           -> playerController.playSong(songId)
        }
    }
}

