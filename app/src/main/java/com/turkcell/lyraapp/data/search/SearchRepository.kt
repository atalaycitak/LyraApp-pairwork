package com.turkcell.lyraapp.data.search

/**
 * Arama ekranı içeriğinin veri kaynağı soyutlaması.
 *
 * Backend REST API'si hazır olmadığından geçici implementasyon [MockSearchRepository]'dir;
 * gerçek API geldiğinde yalnızca implementasyon ve `di/SearchModule.kt` bağlaması değişir
 * (bkz. mvi-overview.md §6).
 */
interface SearchRepository {

    /** Arama ekranının filtrelerini ve tür kartlarını tek seferde döndürür. */
    suspend fun getSearchFeed(): Result<SearchFeed>
}
