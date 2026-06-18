package com.turkcell.lyraapp.ui.components.miniplayer

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

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val playerController: PlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiniPlayerUiState())
    val uiState: StateFlow<MiniPlayerUiState> = _uiState.asStateFlow()

    private val _effect = Channel<MiniPlayerEffect>(Channel.BUFFERED)
    val effect: Flow<MiniPlayerEffect> = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            playerController.playerState.collect { globalState ->
                val isVisible = globalState.songId != null
                val progress = if (globalState.durationMs > 0) {
                    globalState.currentPositionMs.toFloat() / globalState.durationMs.toFloat()
                } else 0f

                _uiState.update {
                    it.copy(
                        isVisible = isVisible,
                        songId = globalState.songId ?: "",
                        trackTitle = globalState.title,
                        artistName = globalState.artist,
                        artworkStartColor = globalState.artworkStartColor,
                        artworkEndColor = globalState.artworkEndColor,
                        isPlaying = globalState.isPlaying,
                        progressPercent = progress.coerceIn(0f, 1f)
                    )
                }
            }
        }
    }

    fun onIntent(intent: MiniPlayerIntent) {
        when (intent) {
            is MiniPlayerIntent.TogglePlayPause -> playerController.togglePlayPause()
            is MiniPlayerIntent.NextSong -> { /* İleri sar / Kuyrukta sonrakine geç mantığı eklenebilir */ }
            is MiniPlayerIntent.ContainerClicked -> {
                val currentSongId = _uiState.value.songId
                if (currentSongId.isNotEmpty()) {
                    viewModelScope.launch {
                        _effect.send(MiniPlayerEffect.NavigateToNowPlaying(currentSongId))
                    }
                }
            }
        }
    }
}
