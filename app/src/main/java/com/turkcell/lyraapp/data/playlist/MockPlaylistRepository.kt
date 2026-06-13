package com.turkcell.lyraapp.data.playlist

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [PlaylistRepository] arayuzunun sahte (fake) implementasyonu.
 * Backend hazir olana kadar kullanilacaktir.
 */
class MockPlaylistRepository @Inject constructor() : PlaylistRepository {

    override suspend fun getAvailableSongs(): Result<List<SelectableSong>> {
        delay(500) // Ag istegini simule et
        return Result.success(
            listOf(
                SelectableSong("s1", "Gece Yarısı", "Mavi Deniz", 0xFF8BC34A, 0xFF33691E), // Yesil gradyan
                SelectableSong("s2", "Sessiz Şehir", "Ela Tuna", 0xFF9575CD, 0xFF4527A0), // Mor gradyan
                SelectableSong("s3", "Yıldız Tozu", "Polaris", 0xFF4DD0E1, 0xFF006064), // Turkuaz
                SelectableSong("s4", "Sahil Yolu", "Kumsal", 0xFFE57373, 0xFFB71C1C), // Kirmizi/Turuncu
                SelectableSong("s5", "Mor Bulutlar", "Derin Kaya", 0xFF4DB6AC, 0xFF004D40), // Yesilimsi turkuaz
                SelectableSong("s6", "İlk Işık", "Sabah Ezgisi", 0xFF64B5F6, 0xFF0D47A1), // Mavi
                SelectableSong("s7", "Kayıp Anlar", "Eko", 0xFF4FC3F7, 0xFF01579B), // Acik Mavi
            )
        )
    }

    override suspend fun createPlaylist(
        name: String,
        description: String,
        isPublic: Boolean,
        songIds: List<String>
    ): Result<Unit> {
        delay(1000) // Olusturma istegini simule et
        return if (name.isBlank()) {
            Result.failure(IllegalArgumentException("Calma listesi adi bos olamaz."))
        } else {
            Result.success(Unit)
        }
    }
}
