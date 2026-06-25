package com.turkcell.lyraapp.data.library

import com.turkcell.lyraapp.data.common.ArtworkPalette
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
import com.turkcell.lyraapp.data.song.SongRepository
import javax.inject.Inject

/**
 * [LibraryRepository]'nin Retrofit tabanli gercek implementasyonu.
 *
 * API'da Library icin ayri bir endpoint olmadigindan bu iterasyonda kutuphane feed'i
 * `/api/v1/songs` yanitindan uretilir. Playlist/album/artist gibi API'da olmayan kaynaklar
 * uydurulmaz; API tasarimi esas alinir.
 */
class RetrofitLibraryRepository @Inject constructor(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
) : LibraryRepository {

    override suspend fun getLibraryFeed(): Result<LibraryFeed> = runCatching {
        val response = songRepository.getSongs(limit = SONG_LIMIT).getOrThrow()
        val songs = response.data

        val myPlaylists = playlistRepository.getMyPlaylists().getOrDefault(emptyList())

        val songItems = songs.map { song ->
            val (startColor, endColor) = ArtworkPalette.colorPairForId(song.id)
            LibraryItem(
                id = song.id,
                title = song.title,
                subtitle = song.artist,
                type = LibraryItemType.Song,
                artworkStartColor = startColor,
                artworkEndColor = endColor,
                isDownloaded = false,
            )
        }

        val playlistItems = myPlaylists.map { playlist ->
            LibraryItem(
                id = playlist.id,
                title = playlist.title,
                subtitle = "Çalma Listesi",
                type = LibraryItemType.Playlist,
                artworkStartColor = playlist.coverStartColor,
                artworkEndColor = playlist.coverEndColor,
                isDownloaded = false,
            )
        }

        LibraryFeed(
            filters = FILTERS,
            quickActions = QUICK_ACTIONS,
            items = playlistItems + songItems,
        )
    }

    private companion object {
        const val SONG_LIMIT = 50

        val FILTERS = listOf(
            LibraryFilter("filter-all", "Tümü", null),
            LibraryFilter("filter-songs", "Şarkılar", LibraryItemType.Song),
        )

        val QUICK_ACTIONS = listOf(
            LibraryQuickAction(
                id = "action-create-playlist",
                title = "Yeni çalma listesi",
                subtitle = "Kapak, ad ve şarkıları belirle",
                type = LibraryQuickActionType.CreatePlaylist,
            ),
            LibraryQuickAction(
                id = "action-downloads",
                title = "İndirilenler",
                subtitle = "Çevrimdışı dinleme yakında",
                type = LibraryQuickActionType.Downloads,
            ),
            LibraryQuickAction(
                id = "action-liked",
                title = "Beğenilen şarkılar",
                subtitle = "Favoriler API kapsamına taşınacak",
                type = LibraryQuickActionType.LikedSongs,
            ),
        )
    }
}
