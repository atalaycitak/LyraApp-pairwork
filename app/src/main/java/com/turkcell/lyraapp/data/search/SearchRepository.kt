package com.turkcell.lyraapp.data.search

/**
 * Arama ekranı içeriğinin veri kaynağı soyutlaması.
 *
 * Aktif implementasyon [RetrofitSearchRepository]'dir; `/api/v1/songs` endpoint'inin `q`
 * parametresiyle sarki arama sonuclari uretilir.
 */
interface SearchRepository {

    /** Arama ekraninin filtrelerini ve API sarki sonuclarini dondurur. */
    suspend fun getSearchFeed(query: String? = null): Result<SearchFeed>
}
