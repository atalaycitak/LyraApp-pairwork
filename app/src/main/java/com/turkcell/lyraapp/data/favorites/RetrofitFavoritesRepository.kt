package com.turkcell.lyraapp.data.favorites

import com.turkcell.lyraapp.data.common.ArtworkPalette
import com.turkcell.lyraapp.data.song.SongDto
import com.turkcell.lyraapp.data.song.SongRepository
import javax.inject.Inject

/**
 * [FavoritesRepository]'nin Retrofit tabanli gercek API uyumlu implementasyonu.
 *
 * API'da favoriler icin ayri bir endpoint bulunmadigindan bu iterasyonda favori sarki listesi
 * `/api/v1/songs` yanitindan turetilir. Kalici favori ekleme/silme davranisi backend endpoint'i
 * gelene kadar kapsam disidir; ViewModel ekranda lokal kaldirma davranisini surdurur.
 */
class RetrofitFavoritesRepository @Inject constructor(
    private val songRepository: SongRepository,
) : FavoritesRepository {

    override suspend fun getFavoritesFeed(): Result<FavoritesFeed> = runCatching {
        val response = songRepository.getSongs(limit = SONG_LIMIT).getOrThrow()
        FavoritesFeed(
            filters = FILTERS,
            items = response.data.map { it.toFavoriteItem() },
        )
    }

    private fun SongDto.toFavoriteItem(): FavoriteItem {
        val (startColor, endColor) = ArtworkPalette.colorPairForId(id)
        return FavoriteItem(
            id = id,
            title = title,
            subtitle = artist,
            type = FavoriteItemType.Song,
            artworkStartColor = startColor,
            artworkEndColor = endColor,
            durationLabel = durationMs.toDurationLabel(),
            isDownloaded = false,
        )
    }

    private fun Long.toDurationLabel(): String {
        val totalSeconds = (this / MILLIS_IN_SECOND).coerceAtLeast(0)
        val minutes = totalSeconds / SECONDS_IN_MINUTE
        val seconds = totalSeconds % SECONDS_IN_MINUTE
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    private companion object {
        const val SONG_LIMIT = 50
        const val MILLIS_IN_SECOND = 1_000L
        const val SECONDS_IN_MINUTE = 60L

        val FILTERS = listOf(
            FavoriteFilter("filter-all", "Tümü", null),
            FavoriteFilter("filter-songs", "Şarkılar", FavoriteItemType.Song),
        )
    }
}
