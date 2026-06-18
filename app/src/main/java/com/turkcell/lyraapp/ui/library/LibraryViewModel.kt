package com.turkcell.lyraapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.library.LibraryItem
import com.turkcell.lyraapp.data.library.LibraryRepository
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
 * Kutuphane ekraninin MVI ViewModel'i.
 *
 * Besleme ekran acilisinda yuklenir. Filtre secimi ViewModel'de islenir; UI yalnizca
 * [LibraryIntent] yayar ve [LibraryUiState] cizer.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LibraryEffect>(Channel.BUFFERED)
    val effect: Flow<LibraryEffect> = _effect.receiveAsFlow()

    private var allItems: List<LibraryItem> = emptyList()

    init {
        loadLibrary()
    }

    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.FilterSelected -> selectFilter(intent.filterId)
            is LibraryIntent.QuickActionClicked -> sendMessageForAction(intent.actionId)
            is LibraryIntent.ItemClicked -> sendMessageForItem(intent.itemId)
            is LibraryIntent.Retry -> loadLibrary()
        }
    }

    private fun loadLibrary() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = libraryRepository.getLibraryFeed()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { feed ->
                    allItems = feed.items
                    val selectedFilterId = LibraryUiState.DEFAULT_FILTER_ID
                    _uiState.update {
                        it.copy(
                            selectedFilterId = selectedFilterId,
                            filters = feed.filters,
                            quickActions = feed.quickActions,
                            items = filterItems(selectedFilterId),
                        )
                    }
                }
                .onFailure { error ->
                    _effect.send(LibraryEffect.ShowError(error.message ?: "Kütüphane yüklenemedi."))
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

    private fun filterItems(filterId: String): List<LibraryItem> {
        val filter = _uiState.value.filters.firstOrNull { it.id == filterId }
        val type = filter?.type
        return if (type == null) {
            allItems
        } else {
            allItems.filter { it.type == type }
        }
    }

    private fun sendMessageForAction(actionId: String) {
        val action = _uiState.value.quickActions.firstOrNull { it.id == actionId } ?: return
        viewModelScope.launch {
            if (action.type == com.turkcell.lyraapp.data.library.LibraryQuickActionType.CreatePlaylist) {
                _effect.send(LibraryEffect.NavigateToCreatePlaylist)
            } else {
                _effect.send(LibraryEffect.ShowMessage("${action.title} özelliği yakında eklenecek."))
            }
        }
    }

    private fun sendMessageForItem(itemId: String) {
        val item = allItems.firstOrNull { it.id == itemId } ?: return
        viewModelScope.launch {
            when (item.type) {
                com.turkcell.lyraapp.data.library.LibraryItemType.Playlist -> {
                    _effect.send(LibraryEffect.NavigateToPlaylistDetail(itemId))
                }
                else -> {
                    _effect.send(LibraryEffect.ShowMessage("${item.title} açılıyor."))
                }
            }
        }
    }
}
