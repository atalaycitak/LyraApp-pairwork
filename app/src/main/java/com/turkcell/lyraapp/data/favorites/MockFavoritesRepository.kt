package com.turkcell.lyraapp.data.favorites

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [FavoritesRepository]'nin MOCK implementasyonu.
 *
 * Ag cagrisi yapmaz; tasarim ve MVI akisinin gelistirilebilmesi icin statik veri dondurur.
 */
class MockFavoritesRepository @Inject constructor() : FavoritesRepository {

    override suspend fun getFavoritesFeed(): Result<FavoritesFeed> {
        delay(NETWORK_DELAY_MS)
        return Result.success(
            FavoritesFeed(
                filters = FILTERS,
                items = ITEMS,
            ),
        )
    }

    private companion object {
        const val NETWORK_DELAY_MS = 650L

        val FILTERS = listOf(
            FavoriteFilter("filter-all", "Tümü", null),
            FavoriteFilter("filter-songs", "Şarkılar", FavoriteItemType.Song),
            FavoriteFilter("filter-albums", "Albümler", FavoriteItemType.Album),
            FavoriteFilter("filter-playlists", "Listeler", FavoriteItemType.Playlist),
        )

        val ITEMS = listOf(
            FavoriteItem(
                id = "favorite-1",
                title = "Gece Sürüşü",
                subtitle = "Mira - Neon Hatıralar",
                type = FavoriteItemType.Song,
                artworkStartColor = 0xFF8B6FB8,
                artworkEndColor = 0xFF4A3D6B,
                durationLabel = "3:42",
                isDownloaded = true,
            ),
            FavoriteItem(
                id = "favorite-2",
                title = "Derin Mavi",
                subtitle = "Okyanus",
                type = FavoriteItemType.Album,
                artworkStartColor = 0xFF6FBF5A,
                artworkEndColor = 0xFF356B2A,
                durationLabel = "12 şarkı",
                isDownloaded = true,
            ),
            FavoriteItem(
                id = "favorite-3",
                title = "Sabah Kahvesi",
                subtitle = "Lyra Mix - 30 şarkı",
                type = FavoriteItemType.Playlist,
                artworkStartColor = 0xFF7C83D9,
                artworkEndColor = 0xFF3E4486,
                durationLabel = "1 sa 48 dk",
                isDownloaded = false,
            ),
            FavoriteItem(
                id = "favorite-4",
                title = "Polaris",
                subtitle = "Nova - Kuzey Işıkları",
                type = FavoriteItemType.Song,
                artworkStartColor = 0xFF3D5A80,
                artworkEndColor = 0xFF1B2A45,
                durationLabel = "4:08",
                isDownloaded = false,
            ),
            FavoriteItem(
                id = "favorite-5",
                title = "Neon Sokaklar",
                subtitle = "Şehir Işıkları",
                type = FavoriteItemType.Album,
                artworkStartColor = 0xFFD98E4A,
                artworkEndColor = 0xFF8A5526,
                durationLabel = "9 şarkı",
                isDownloaded = false,
            ),
            FavoriteItem(
                id = "favorite-6",
                title = "Haftalık Keşif",
                subtitle = "Lyra - Senin için seçildi",
                type = FavoriteItemType.Playlist,
                artworkStartColor = 0xFF9B7FC4,
                artworkEndColor = 0xFF5A4480,
                durationLabel = "2 sa 12 dk",
                isDownloaded = true,
            ),
        )
    }
}
