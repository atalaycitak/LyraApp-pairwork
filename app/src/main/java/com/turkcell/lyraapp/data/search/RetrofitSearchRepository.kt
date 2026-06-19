package com.turkcell.lyraapp.data.search

import com.turkcell.lyraapp.data.common.ArtworkPalette
import com.turkcell.lyraapp.data.song.SongDto
import com.turkcell.lyraapp.data.song.SongRepository
import javax.inject.Inject

/**
 * [SearchRepository]'nin Retrofit tabanli gercek implementasyonu.
 *
 * API aramasi `/api/v1/songs?q=...` uzerinden yapilir. API'da tur/genre endpoint'i olmadigindan
 * sahte tur karti uretilmez; ekran sarki sonuclarina odaklanir.
 */
class RetrofitSearchRepository @Inject constructor(
    private val songRepository: SongRepository,
) : SearchRepository {

    override suspend fun getSearchFeed(query: String?): Result<SearchFeed> = runCatching {
        val normalizedQuery = query?.trim()?.takeIf { it.isNotEmpty() }
        val response = songRepository.getSongs(
            limit = SONG_LIMIT,
            q = normalizedQuery,
        ).getOrThrow()

        SearchFeed(
            filters = FILTERS,
            genres = emptyList(),
            results = response.data.map { it.toSearchResultItem() },
        )
    }

    private fun SongDto.toSearchResultItem(): SearchResultItem {
        val (startColor, endColor) = ArtworkPalette.colorPairForId(id)
        return SearchResultItem(
            id = id,
            title = title,
            subtitle = listOfNotNull(artist, album).joinToString(" - "),
            artworkStartColor = startColor,
            artworkEndColor = endColor,
            durationLabel = durationMs.toDurationLabel(),
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
            SearchFilter("filter-all", "Tümü"),
            SearchFilter("filter-songs", "Şarkılar"),
        )
    }
}
