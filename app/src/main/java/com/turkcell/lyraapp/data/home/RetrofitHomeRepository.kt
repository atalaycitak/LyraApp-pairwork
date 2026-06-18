package com.turkcell.lyraapp.data.home

import com.turkcell.lyraapp.data.common.ArtworkPalette
import com.turkcell.lyraapp.data.song.SongRepository
import javax.inject.Inject

/**
 * [HomeRepository]'nin Retrofit tabanlı gerçek implementasyonu.
 *
 * [SongRepository.getSongs] çağırarak şarkı listesini alır ve [HomeFeed]'e dönüştürür.
 * API'da kapak görseli alanı bulunmadığından her şarkı için şarkı ID'sinin hash değerinden
 * deterministik renk çifti üretilir; aynı ID her zaman aynı rengi verir.
 *
 * - [QuickPick]: ilk 6 şarkı (grid görünümü).
 * - [RecentlyPlayed]: tüm şarkılar (sanatçı adı subtitle olarak kullanılır).
 * - [PlaylistsForYou]: bu iterasyonda boş liste; playlist entegrasyonu ayrı iterasyonda.
 */
class RetrofitHomeRepository @Inject constructor(
    private val songRepository: SongRepository,
) : HomeRepository {

    override suspend fun getHomeFeed(): Result<HomeFeed> = runCatching {
        val response = songRepository.getSongs(limit = 50).getOrThrow()
        val songs = response.data

        val quickPicks = songs.take(6).map { song ->
            val (start, end) = ArtworkPalette.colorPairForId(song.id)
            QuickPick(
                id = song.id,
                title = song.title,
                artworkStartColor = start,
                artworkEndColor = end,
            )
        }

        val recentlyPlayed = songs.map { song ->
            val (start, end) = ArtworkPalette.colorPairForId(song.id)
            RecentlyPlayed(
                id = song.id,
                title = song.title,
                subtitle = song.artist,
                artworkStartColor = start,
                artworkEndColor = end,
            )
        }

        HomeFeed(
            userInitials = "",
            quickPicks = quickPicks,
            recentlyPlayed = recentlyPlayed,
            playlistsForYou = emptyList(),
        )
    }
}
