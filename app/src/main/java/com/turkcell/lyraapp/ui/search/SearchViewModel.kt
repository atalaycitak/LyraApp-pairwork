package com.turkcell.lyraapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.search.SearchRepository
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
 * Arama ekranının ViewModel'i (bkz. mvi-viewmodel-rules.md).
 *
 * Besleme (filtreler + türler), ekran açılışında bir kez yüklenir; başarısızlıkta
 * [SearchIntent.Retry] ile yeniden denenir. Filtre çipi ve arama sorgusu değişimleri
 * state'i atomik olarak günceller.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SearchEffect>(Channel.BUFFERED)
    val effect: Flow<SearchEffect> = _effect.receiveAsFlow()

    init {
        loadFeed()
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> _uiState.update { it.copy(searchQuery = intent.value) }
            is SearchIntent.FilterSelected -> _uiState.update { it.copy(selectedFilterId = intent.filterId) }
            is SearchIntent.Retry -> loadFeed()
        }
    }

    private fun loadFeed() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = searchRepository.getSearchFeed()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { feed ->
                    _uiState.update {
                        it.copy(
                            filters = feed.filters,
                            genres = feed.genres,
                        )
                    }
                }
                .onFailure { error ->
                    _effect.send(SearchEffect.ShowError(error.message ?: "Arama ekranı yüklenemedi."))
                }
        }
    }
}
