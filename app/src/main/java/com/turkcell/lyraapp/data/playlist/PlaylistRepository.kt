package com.turkcell.lyraapp.data.playlist

/**
 * Calma listesi islemleri icin repository arayuzu.
 */
interface PlaylistRepository {

    /** Calma listesine eklenebilecek sarki listesini dondurur. */
    suspend fun getAvailableSongs(): Result<List<SelectableSong>>

    /**
     * Yeni bir calma listesi olusturur.
     * @param name Calma listesi adi
     * @param description Istege bagli aciklama
     * @param isPublic Herkese acik (true) veya gizli (false)
     * @param songIds Eklenen sarkilarin ID listesi
     */
    suspend fun createPlaylist(
        name: String,
        description: String,
        isPublic: Boolean,
        songIds: List<String>
    ): Result<Unit>
}
