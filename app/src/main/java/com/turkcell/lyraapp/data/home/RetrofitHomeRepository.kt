package com.turkcell.lyraapp.data.home

import com.turkcell.lyraapp.data.common.ArtworkPalette
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * [HomeRepository]'nin Retrofit tabanlı gerçek implementasyonu.
 *
 * Yeni `/me` endpoint'lerinden eş zamanlı (concurrent) olarak verileri çeker
 * ve UI'ın çizebileceği [HomeFeed] formatına dönüştürür.
 * API'da kapak görseli alanı bulunmadığından her şarkı için şarkı ID'sinin hash değerinden
 * deterministik renk çifti üretilir; aynı ID her zaman aynı rengi verir.
 */
class RetrofitHomeRepository @Inject constructor(
    private val homeApiService: HomeApiService,
) : HomeRepository {

    override suspend fun getHomeFeed(): Result<HomeFeed> = runCatching {
        coroutineScope {
            val recentlyPlayedDeferred = async { homeApiService.getRecentlyPlayed() }
            val forYouDeferred = async { homeApiService.getForYou() }
            val recommendationsDeferred = async { homeApiService.getRecommendations() }

            val recentlyPlayedResponse = recentlyPlayedDeferred.await()
            val forYouResponse = forYouDeferred.await()
            val recommendationsResponse = recommendationsDeferred.await()

            val quickPicks = recommendationsResponse.data.take(6).map { song ->
                val (start, end) = ArtworkPalette.colorPairForId(song.id)
                QuickPick(
                    id = song.id,
                    title = song.title,
                    artworkStartColor = start,
                    artworkEndColor = end,
                )
            }

            val recentlyPlayed = recentlyPlayedResponse.data.take(15).map { song ->
                val (start, end) = ArtworkPalette.colorPairForId(song.id)
                RecentlyPlayed(
                    id = song.id,
                    title = song.title,
                    subtitle = song.artist,
                    artworkStartColor = start,
                    artworkEndColor = end,
                )
            }

            val forYou = forYouResponse.data.take(15).map { song ->
                val (start, end) = ArtworkPalette.colorPairForId(song.id)
                ForYouSong(
                    id = song.id,
                    title = song.title,
                    artworkStartColor = start,
                    artworkEndColor = end,
                )
            }

            HomeFeed(
                userInitials = "",
                quickPicks = quickPicks,
                recentlyPlayed = recentlyPlayed,
                forYou = forYou,
            )
        }
    }
}

