package com.turkcell.lyraapp.ui.favorites

import com.turkcell.lyraapp.data.favorites.FavoriteFilter
import com.turkcell.lyraapp.data.favorites.FavoriteItem

/**
 * Favorites ekraninin MVI sozlesmesi: UiState + Intent + Effect.
 */
data class FavoritesUiState(
    val isLoading: Boolean = false,
    val selectedFilterId: String = DEFAULT_FILTER_ID,
    val filters: List<FavoriteFilter> = emptyList(),
    val items: List<FavoriteItem> = emptyList(),
) {
    companion object {
        const val DEFAULT_FILTER_ID = "filter-all"
    }
}

sealed interface FavoritesIntent {
    data class FilterSelected(val filterId: String) : FavoritesIntent
    data class ItemClicked(val itemId: String) : FavoritesIntent
    data class FavoriteClicked(val itemId: String) : FavoritesIntent
    data object Retry : FavoritesIntent
}

sealed interface FavoritesEffect {
    data class NavigateToPlayer(val songId: String) : FavoritesEffect
    data class NavigateToPlaylistDetail(val playlistId: String) : FavoritesEffect
    data class ShowMessage(val message: String) : FavoritesEffect
    data class ShowError(val message: String) : FavoritesEffect
}
