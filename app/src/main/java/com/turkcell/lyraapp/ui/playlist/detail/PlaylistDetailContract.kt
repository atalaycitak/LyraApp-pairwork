package com.turkcell.lyraapp.ui.playlist.detail

import com.turkcell.lyraapp.data.playlist.PlaylistDetailModel

data class PlaylistDetailUiState(
    val isLoading: Boolean = false,
    val playlistDetail: PlaylistDetailModel? = null
)

sealed interface PlaylistDetailIntent {
    data class LoadPlaylist(val playlistId: String) : PlaylistDetailIntent
    data class OnSongClick(val songId: String) : PlaylistDetailIntent
    data class OnLikeSongClick(val songId: String) : PlaylistDetailIntent
    data object OnPlayClick : PlaylistDetailIntent
    data object OnShuffleClick : PlaylistDetailIntent
    data object OnDownloadClick : PlaylistDetailIntent
    data object OnAddClick : PlaylistDetailIntent
    data object OnBackClick : PlaylistDetailIntent
    data object OnMoreClick : PlaylistDetailIntent
    data class OnRemoveSongClick(val songId: String) : PlaylistDetailIntent
}

sealed interface PlaylistDetailEffect {
    data object NavigateBack : PlaylistDetailEffect
    data class NavigateToPlayer(val songId: String) : PlaylistDetailEffect
    data class ShowSnackbar(val message: String) : PlaylistDetailEffect
}
