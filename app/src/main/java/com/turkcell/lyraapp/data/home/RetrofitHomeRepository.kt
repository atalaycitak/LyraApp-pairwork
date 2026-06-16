package com.turkcell.lyraapp.data.home

import com.turkcell.lyraapp.data.song.SongRepository
import javax.inject.Inject
import kotlin.math.abs

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
            val (start, end) = colorPairForId(song.id)
            QuickPick(
                id = song.id,
                title = song.title,
                artworkStartColor = start,
                artworkEndColor = end,
            )
        }

        val recentlyPlayed = songs.map { song ->
            val (start, end) = colorPairForId(song.id)
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

    /**
     * Şarkı ID'sinden deterministik renk çifti türetir.
     *
     * Sabit renk paleti üzerinde ID.hashCode() modulo uygulanır; böylece
     * aynı ID her zaman aynı rengi verir (rastgele değil, deterministik).
     */
    private fun colorPairForId(id: String): Pair<Long, Long> {
        val palette = ARTWORK_PALETTE
        val index = abs(id.hashCode()) % palette.size
        return palette[index]
    }

    private companion object {
        val ARTWORK_PALETTE = listOf(
            Pair(0xFF8B6FB8L, 0xFF4A3D6BL),
            Pair(0xFF7C83D9L, 0xFF3E4486L),
            Pair(0xFFD98E4AL, 0xFF8A5526L),
            Pair(0xFF4AC2A8L, 0xFF1F6E5CL),
            Pair(0xFF6FBF5AL, 0xFF356B2AL),
            Pair(0xFF5AAFC9L, 0xFF2A5F73L),
            Pair(0xFF9B7FC4L, 0xFF5A4480L),
            Pair(0xFF6B5FB8L, 0xFF3A3270L),
            Pair(0xFF3FAE9CL, 0xFF1E5D52L),
            Pair(0xFFD9604AL, 0xFF8A3020L),
            Pair(0xFF4A8BD9L, 0xFF1E4580L),
            Pair(0xFFD9A84AL, 0xFF8A6020L),
        )
    }
}
