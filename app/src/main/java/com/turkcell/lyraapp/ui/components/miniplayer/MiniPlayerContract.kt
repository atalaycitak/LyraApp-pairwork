package com.turkcell.lyraapp.ui.components.miniplayer

data class MiniPlayerUiState(
    val isVisible: Boolean = false,
    val songId: String = "",
    val trackTitle: String = "",
    val artistName: String = "",
    val artworkStartColor: Long = 0xFF8B6FB8L,
    val artworkEndColor: Long = 0xFF4A3D6BL,
    val isPlaying: Boolean = false,
    val progressPercent: Float = 0f
)

sealed interface MiniPlayerIntent {
    data object TogglePlayPause : MiniPlayerIntent
    data object NextSong : MiniPlayerIntent
    data object ContainerClicked : MiniPlayerIntent
}

sealed interface MiniPlayerEffect {
    data class NavigateToNowPlaying(val songId: String) : MiniPlayerEffect
}
