package com.turkcell.lyraapp.data.library

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [LibraryRepository]'nin MOCK implementasyonu.
 *
 * Ag cagrisi yapmaz; tasarim ve MVI akisinin gelistirilebilmesi icin statik veri dondurur.
 * Gercek API geldiginde bu sinif ag tabanli implementasyonla degistirilir.
 */
class MockLibraryRepository @Inject constructor() : LibraryRepository {

    override suspend fun getLibraryFeed(): Result<LibraryFeed> {
        delay(NETWORK_DELAY_MS)
        return Result.success(
            LibraryFeed(
                filters = FILTERS,
                quickActions = QUICK_ACTIONS,
                items = ITEMS,
            ),
        )
    }

    private companion object {
        const val NETWORK_DELAY_MS = 700L

        val FILTERS = listOf(
            LibraryFilter("filter-all", "Tumu", null),
            LibraryFilter("filter-playlists", "Calma listeleri", LibraryItemType.Playlist),
            LibraryFilter("filter-albums", "Albumler", LibraryItemType.Album),
            LibraryFilter("filter-artists", "Sanatcilar", LibraryItemType.Artist),
        )

        val QUICK_ACTIONS = listOf(
            LibraryQuickAction(
                id = "action-create-playlist",
                title = "Yeni calma listesi",
                subtitle = "Kapak, ad ve sarkilari belirle",
                type = LibraryQuickActionType.CreatePlaylist,
            ),
            LibraryQuickAction(
                id = "action-downloads",
                title = "Indirilenler",
                subtitle = "Cevrimdisi dinlemeye hazir",
                type = LibraryQuickActionType.Downloads,
            ),
            LibraryQuickAction(
                id = "action-liked",
                title = "Begenilen sarkilar",
                subtitle = "Favori parcali koleksiyon",
                type = LibraryQuickActionType.LikedSongs,
            ),
        )

        val ITEMS = listOf(
            LibraryItem(
                id = "library-1",
                title = "Gece Surusu",
                subtitle = "Calma listesi - 42 sarki",
                type = LibraryItemType.Playlist,
                artworkStartColor = 0xFF8B6FB8,
                artworkEndColor = 0xFF4A3D6B,
                isDownloaded = true,
            ),
            LibraryItem(
                id = "library-2",
                title = "Haftalik Kesif",
                subtitle = "Calma listesi - Lyra",
                type = LibraryItemType.Playlist,
                artworkStartColor = 0xFF9B7FC4,
                artworkEndColor = 0xFF5A4480,
                isDownloaded = false,
            ),
            LibraryItem(
                id = "library-3",
                title = "Derin Mavi",
                subtitle = "Album - Okyanus",
                type = LibraryItemType.Album,
                artworkStartColor = 0xFF6FBF5A,
                artworkEndColor = 0xFF356B2A,
                isDownloaded = true,
            ),
            LibraryItem(
                id = "library-4",
                title = "Neon Sokaklar",
                subtitle = "Album - Sehir Isiklari",
                type = LibraryItemType.Album,
                artworkStartColor = 0xFFD98E4A,
                artworkEndColor = 0xFF8A5526,
                isDownloaded = false,
            ),
            LibraryItem(
                id = "library-5",
                title = "Polaris",
                subtitle = "Sanatci - 128 bin dinleyici",
                type = LibraryItemType.Artist,
                artworkStartColor = 0xFF3D5A80,
                artworkEndColor = 0xFF1B2A45,
                isDownloaded = false,
            ),
            LibraryItem(
                id = "library-6",
                title = "Sabah Kahvesi",
                subtitle = "Calma listesi - 30 sarki",
                type = LibraryItemType.Playlist,
                artworkStartColor = 0xFF7C83D9,
                artworkEndColor = 0xFF3E4486,
                isDownloaded = true,
            ),
        )
    }
}
