package com.turkcell.lyraapp.ui.playlist.create

import com.turkcell.lyraapp.data.playlist.SelectableSong

/**
 * Calma listesi olusturma ekraninin MVI sozlesmesi: State + Intent + Effect.
 */
data class CreatePlaylistUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val availableSongs: List<SelectableSong> = emptyList(),
    val selectedSongIds: Set<String> = emptySet(),
    val isSaveEnabled: Boolean = false, // Turetilir (name bossa false)
    val selectedCountText: String = "0 seçili", // Turetilir
)

sealed interface CreatePlaylistIntent {
    data class NameChanged(val value: String) : CreatePlaylistIntent
    data class DescriptionChanged(val value: String) : CreatePlaylistIntent
    data object CoverClicked : CreatePlaylistIntent
    data class PrivacyToggled(val isPublic: Boolean) : CreatePlaylistIntent
    data class SongSelectionToggled(val songId: String) : CreatePlaylistIntent
    data object SaveClicked : CreatePlaylistIntent
    data object CloseClicked : CreatePlaylistIntent
}

sealed interface CreatePlaylistEffect {
    data object NavigateBack : CreatePlaylistEffect
    data class ShowMessage(val message: String) : CreatePlaylistEffect
    data class ShowError(val message: String) : CreatePlaylistEffect
}
