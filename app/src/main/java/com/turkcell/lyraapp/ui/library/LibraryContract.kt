package com.turkcell.lyraapp.ui.library

import com.turkcell.lyraapp.data.library.LibraryFilter
import com.turkcell.lyraapp.data.library.LibraryItem
import com.turkcell.lyraapp.data.library.LibraryQuickAction

/**
 * Kutuphane ekraninin MVI sozlesmesi: UiState + Intent + Effect.
 */
data class LibraryUiState(
    val isLoading: Boolean = false,
    val selectedFilterId: String = DEFAULT_FILTER_ID,
    val filters: List<LibraryFilter> = emptyList(),
    val quickActions: List<LibraryQuickAction> = emptyList(),
    val items: List<LibraryItem> = emptyList(),
) {
    companion object {
        const val DEFAULT_FILTER_ID = "filter-all"
    }
}

sealed interface LibraryIntent {
    data class FilterSelected(val filterId: String) : LibraryIntent
    data class QuickActionClicked(val actionId: String) : LibraryIntent
    data class ItemClicked(val itemId: String) : LibraryIntent
    data object Retry : LibraryIntent
}

sealed interface LibraryEffect {
    data object NavigateToCreatePlaylist : LibraryEffect
    data class NavigateToPlaylistDetail(val playlistId: String) : LibraryEffect
    data class NavigateToPlayer(val songId: String) : LibraryEffect
    data class ShowMessage(val message: String) : LibraryEffect
    data class ShowError(val message: String) : LibraryEffect
}
