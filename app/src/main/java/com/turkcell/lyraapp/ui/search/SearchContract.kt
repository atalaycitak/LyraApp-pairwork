package com.turkcell.lyraapp.ui.search

import com.turkcell.lyraapp.data.search.Genre
import com.turkcell.lyraapp.data.search.SearchFilter

/**
 * Arama ekranının MVI sözleşmesi: UiState + Intent + Effect (bkz. mvi-contracts.md).
 *
 * [selectedFilterId] kullanıcının seçtiği filtre çipinin kimliğidir; varsayılan olarak
 * "filter-all" (Hepsi) seçilidir. [searchQuery] arama alanındaki metni temsil eder.
 */
data class SearchUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedFilterId: String = "filter-all",
    val filters: List<SearchFilter> = emptyList(),
    val genres: List<Genre> = emptyList(),
)

/**
 * Kullanıcıdan gelen niyetler. UI yalnızca bu tipleri yayımlar; iş mantığını çalıştırmaz.
 */
sealed interface SearchIntent {
    /** Arama alanındaki metin değişti. */
    data class QueryChanged(val value: String) : SearchIntent

    /** Filtre çipine tıklandı. */
    data class FilterSelected(val filterId: String) : SearchIntent

    /** Besleme yüklemesi başarısız olduğunda kullanıcı yeniden dener. */
    data object Retry : SearchIntent
}

/**
 * Tek seferlik (one-shot) olaylar: snackbar vb. State içinde tutulmaz,
 * böylece konfigürasyon değişiminde tekrar tetiklenmez.
 */
sealed interface SearchEffect {
    data class ShowError(val message: String) : SearchEffect
}
