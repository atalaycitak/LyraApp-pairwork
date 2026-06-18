package com.turkcell.lyraapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.favorites.FavoriteItem
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
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
 * Favorites ekraninin MVI ViewModel'i.
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _effect = Channel<FavoritesEffect>(Channel.BUFFERED)
    val effect: Flow<FavoritesEffect> = _effect.receiveAsFlow()

    private var allItems: List<FavoriteItem> = emptyList()

    init {
        loadFavorites()
    }

    fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.FilterSelected -> selectFilter(intent.filterId)
            is FavoritesIntent.ItemClicked -> sendMessageForItem(intent.itemId)
            is FavoritesIntent.FavoriteClicked -> removeFavorite(intent.itemId)
            is FavoritesIntent.Retry -> loadFavorites()
        }
    }

    private fun loadFavorites() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = favoritesRepository.getFavoritesFeed()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { feed ->
                    allItems = feed.items
                    val selectedFilterId = FavoritesUiState.DEFAULT_FILTER_ID
                    _uiState.update {
                        it.copy(
                            selectedFilterId = selectedFilterId,
                            filters = feed.filters,
                            items = filterItems(selectedFilterId),
                        )
                    }
                }
                .onFailure { error ->
                    _effect.send(FavoritesEffect.ShowError(error.message ?: "Favoriler yüklenemedi."))
                }
        }
    }

    private fun selectFilter(filterId: String) {
        _uiState.update {
            it.copy(
                selectedFilterId = filterId,
                items = filterItems(filterId),
            )
        }
    }

    private fun filterItems(filterId: String): List<FavoriteItem> {
        val filter = _uiState.value.filters.firstOrNull { it.id == filterId }
        val type = filter?.type
        return if (type == null) {
            allItems
        } else {
            allItems.filter { it.type == type }
        }
    }

    private fun sendMessageForItem(itemId: String) {
        val item = allItems.firstOrNull { it.id == itemId } ?: return
        viewModelScope.launch {
            when (item.type) {
                com.turkcell.lyraapp.data.favorites.FavoriteItemType.Song -> {
                    _effect.send(FavoritesEffect.NavigateToPlayer(itemId))
                }
                com.turkcell.lyraapp.data.favorites.FavoriteItemType.Playlist -> {
                    _effect.send(FavoritesEffect.NavigateToPlaylistDetail(itemId))
                }
                else -> {
                    _effect.send(FavoritesEffect.ShowMessage("${item.title} açılıyor."))
                }
            }
        }
    }

    private fun removeFavorite(itemId: String) {
        val removedItem = allItems.firstOrNull { it.id == itemId } ?: return
        allItems = allItems.filterNot { it.id == itemId }
        _uiState.update {
            it.copy(items = filterItems(it.selectedFilterId))
        }
        viewModelScope.launch {
            _effect.send(FavoritesEffect.ShowMessage("${removedItem.title} favorilerden kaldırıldı."))
        }
    }
}
