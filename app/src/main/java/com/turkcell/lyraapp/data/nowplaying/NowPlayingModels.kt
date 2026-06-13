package com.turkcell.lyraapp.data.nowplaying

/**
 * Now Playing ekranının veri modelleri.
 *
 * Kapak görselleri henüz bir CDN/görsel servisi olmadığından gradyan renk çifti
 * (`artworkStartColor`/`artworkEndColor`, ARGB hex) ile temsil edilir. Gerçek API
 * geldiğinde bu alanlar görsel URL'siyle değiştirilebilir; UI katmanı yalnızca
 * bu modeli çizer (bkz. docs/decisions.md — Stub Repository deseni).
 */

/** Çalmakta olan parçanın bilgilerini taşıyan model. */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val playlistName: String,
    val durationMs: Long,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

/**
 * Repository'den dönen aggregate model: mevcut parça ve favori durumu.
 *
 * [isFavorite] kullanıcının bu parçayı favorilerine ekleyip eklemediğini belirtir.
 */
data class NowPlayingInfo(
    val track: Track,
    val isFavorite: Boolean,
)
