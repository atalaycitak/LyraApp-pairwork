package com.turkcell.lyraapp.ui.playlist.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
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
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PlaylistDetailEffect>(Channel.BUFFERED)
    val effect: Flow<PlaylistDetailEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.LoadPlaylist -> loadPlaylist(intent.playlistId)
            is PlaylistDetailIntent.OnSongClick -> navigateToPlayer(intent.songId)
            is PlaylistDetailIntent.OnLikeSongClick -> toggleLikeSong(intent.songId)
            PlaylistDetailIntent.OnAddClick -> showSnackbar("Listeye eklendi")
            PlaylistDetailIntent.OnBackClick -> sendEffect(PlaylistDetailEffect.NavigateBack)
            PlaylistDetailIntent.OnDownloadClick -> showSnackbar("İndirme başlatıldı")
            PlaylistDetailIntent.OnMoreClick -> showSnackbar("Daha fazla seçenek")
            PlaylistDetailIntent.OnPlayClick -> playPlaylist()
            PlaylistDetailIntent.OnShuffleClick -> shufflePlaylist()
            is PlaylistDetailIntent.OnRemoveSongClick -> removeSong(intent.songId)
            PlaylistDetailIntent.OnRenameClick -> {
                _uiState.update { it.copy(showRenameDialog = true) }
            }
            PlaylistDetailIntent.OnDismissRenameDialog -> {
                _uiState.update { it.copy(showRenameDialog = false) }
            }
            is PlaylistDetailIntent.OnConfirmRename -> renamePlaylist(intent.newName)
        }
    }

    private fun playPlaylist() {
        val detail = _uiState.value.playlistDetail ?: return
        if (detail.songs.isNotEmpty()) {
            navigateToPlayer(detail.songs.first().id)
        } else {
            showSnackbar("Çalma listesi boş.")
        }
    }

    private fun shufflePlaylist() {
        val detail = _uiState.value.playlistDetail ?: return
        if (detail.songs.isNotEmpty()) {
            navigateToPlayer(detail.songs.random().id)
        } else {
            showSnackbar("Çalma listesi boş.")
        }
    }

    private fun renamePlaylist(newName: String) {
        val playlistId = currentPlaylistId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRenaming = true) }
            val result = playlistRepository.renamePlaylist(playlistId, newName)
            result.onSuccess {
                _uiState.update { it.copy(isRenaming = false, showRenameDialog = false) }
                sendEffect(PlaylistDetailEffect.ShowSnackbar("Çalma listesi ismi değiştirildi"))
                // Yeniden yükle
                onIntent(PlaylistDetailIntent.LoadPlaylist(playlistId))
            }.onFailure { error ->
                _uiState.update { it.copy(isRenaming = false) }
                sendEffect(PlaylistDetailEffect.ShowSnackbar(error.message ?: "İsim değiştirilemedi"))
            }
        }
    }

    private fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = playlistRepository.getPlaylistDetail(playlistId)
            result.fold(
                onSuccess = { detail ->
                    _uiState.update { it.copy(isLoading = false, playlistDetail = detail) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    sendEffect(PlaylistDetailEffect.ShowSnackbar(error.message ?: "Çalma listesi yüklenemedi."))
                }
            )
        }
    }

    private fun toggleLikeSong(songId: String) {
        _uiState.update { state ->
            val detail = state.playlistDetail ?: return@update state
            val updatedSongs = detail.songs.map { song ->
                if (song.id == songId) song.copy(isLiked = !song.isLiked) else song
            }
            state.copy(playlistDetail = detail.copy(songs = updatedSongs))
        }
    }

    private fun navigateToPlayer(songId: String) {
        sendEffect(PlaylistDetailEffect.NavigateToPlayer(songId))
    }

    private fun removeSong(songId: String) {
        val detail = _uiState.value.playlistDetail ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            playlistRepository.removeSongFromPlaylist(detail.id, songId)
                .onSuccess {
                    showSnackbar("Şarkı listeden çıkarıldı.")
                    loadPlaylist(detail.id) // Yeniden yükle
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    showSnackbar("Şarkı çıkarılamadı (Yetkiniz olmayabilir).")
                }
        }
    }

    private fun showSnackbar(message: String) {
        sendEffect(PlaylistDetailEffect.ShowSnackbar(message))
    }

    private fun sendEffect(effect: PlaylistDetailEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
