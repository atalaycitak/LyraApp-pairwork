package com.turkcell.lyraapp.data.search

/**
 * Arama ekranının veri modelleri.
 *
 * Kapak görselleri henüz bir CDN/görsel servisi olmadığından gradyan renk çifti
 * (`artworkStartColor`/`artworkEndColor`, ARGB hex) ile temsil edilir. Gerçek API
 * geldiğinde bu alanlar görsel URL'siyle değiştirilebilir; UI katmanı yalnızca
 * bu modeli çizer (bkz. docs/decisions.md — Stub Repository deseni).
 */

/** Arama ekranındaki filtre çiplerinin veri modeli. */
data class SearchFilter(
    val id: String,
    val label: String,
)

/** "Türlere göz at" bölümündeki tür kartı. */
data class Genre(
    val id: String,
    val name: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

/** API'dan gelen sarki arama sonucunun UI tarafinda kullanilan modeli. */
data class SearchResultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
    val durationLabel: String,
)

/** Repository'den donen aggregate model: filtreler + sarki sonuclari. */
data class SearchFeed(
    val filters: List<SearchFilter>,
    val genres: List<Genre>,
    val results: List<SearchResultItem> = emptyList(),
)
