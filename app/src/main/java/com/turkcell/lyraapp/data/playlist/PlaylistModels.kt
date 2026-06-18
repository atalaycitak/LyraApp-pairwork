package com.turkcell.lyraapp.data.playlist

/**
 * Calma listesi olusturma ekraninda gosterilecek sarki modeli.
 */
data class SelectableSong(
    val id: String,
    val title: String,
    val artist: String,
    val coverStartColor: Long,
    val coverEndColor: Long,
)

/**
 * Çalma listesi detay ekranındaki bir şarkıyı temsil eder.
 */
data class SongItem(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val coverStartColor: Long,
    val coverEndColor: Long,
    val isLiked: Boolean = false,
    val isPlaying: Boolean = false
)

/**
 * Çalma listesi detay ekranı için gereken tüm verileri içerir.
 */
data class PlaylistDetailModel(
    val id: String,
    val title: String,
    val description: String,
    val creator: String,
    val songCount: Int,
    val totalDuration: String,
    val coverStartColor: Long,
    val coverEndColor: Long,
    val songs: List<SongItem>
)
