package com.turkcell.lyraapp.ui.playlist.create

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
class CreatePlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlaylistUiState())
    val uiState: StateFlow<CreatePlaylistUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CreatePlaylistEffect>(Channel.BUFFERED)
    val effect: Flow<CreatePlaylistEffect> = _effect.receiveAsFlow()

    init {
        loadAvailableSongs()
    }

    fun onIntent(intent: CreatePlaylistIntent) {
        when (intent) {
            is CreatePlaylistIntent.NameChanged -> updateState { it.copy(name = intent.value) }
            is CreatePlaylistIntent.DescriptionChanged -> updateState { it.copy(description = intent.value) }
            is CreatePlaylistIntent.CoverClicked -> sendEffect(CreatePlaylistEffect.ShowMessage("Kapak ekleme özelliği yakında eklenecek."))
            is CreatePlaylistIntent.PrivacyToggled -> updateState { it.copy(isPublic = intent.isPublic) }
            is CreatePlaylistIntent.SongSelectionToggled -> toggleSongSelection(intent.songId)
            is CreatePlaylistIntent.SaveClicked -> savePlaylist()
            is CreatePlaylistIntent.CloseClicked -> sendEffect(CreatePlaylistEffect.NavigateBack)
        }
    }

    private fun loadAvailableSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            playlistRepository.getAvailableSongs()
                .onSuccess { songs ->
                    updateState { it.copy(isLoading = false, availableSongs = songs) }
                }
                .onFailure {
                    _uiState.update { state -> state.copy(isLoading = false) }
                    sendEffect(CreatePlaylistEffect.ShowError("Şarkılar yüklenemedi."))
                }
        }
    }

    private fun toggleSongSelection(songId: String) {
        updateState { current ->
            val newSelection = current.selectedSongIds.toMutableSet()
            if (newSelection.contains(songId)) {
                newSelection.remove(songId)
            } else {
                newSelection.add(songId)
            }
            current.copy(selectedSongIds = newSelection)
        }
    }

    private fun savePlaylist() {
        val state = _uiState.value
        if (!state.isSaveEnabled || state.isLoading) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            playlistRepository.createPlaylist(
                name = state.name,
                description = state.description,
                isPublic = state.isPublic,
                songIds = state.selectedSongIds.toList()
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                sendEffect(CreatePlaylistEffect.NavigateBack)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                sendEffect(CreatePlaylistEffect.ShowError(error.message ?: "Oluşturma başarısız oldu."))
            }
        }
    }

    private fun updateState(transform: (CreatePlaylistUiState) -> CreatePlaylistUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(
                isSaveEnabled = updated.name.isNotBlank(),
                selectedCountText = "${updated.selectedSongIds.size} seçili",
            )
        }
    }

    private fun sendEffect(effect: CreatePlaylistEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
