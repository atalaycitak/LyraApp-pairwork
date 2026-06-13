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
