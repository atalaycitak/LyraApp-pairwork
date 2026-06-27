package com.turkcell.lyraapp.data.playlist

import com.turkcell.lyraapp.data.common.ArtworkPalette
import com.turkcell.lyraapp.data.song.SongDto
import com.turkcell.lyraapp.data.song.SongRepository
import javax.inject.Inject

class RetrofitPlaylistRepository @Inject constructor(
    private val playlistApiService: PlaylistApiService,
    private val songRepository: SongRepository,
) : PlaylistRepository {

    override suspend fun getAvailableSongs(): Result<List<SelectableSong>> = runCatching {
        songRepository.getSongs(limit = SONG_LIMIT).getOrThrow().data.map { song ->
            val (startColor, endColor) = ArtworkPalette.colorPairForId(song.id)
            SelectableSong(
                id = song.id,
                title = song.title,
                artist = song.artist,
                coverStartColor = startColor,
                coverEndColor = endColor,
            )
        }
    }

    override suspend fun createPlaylist(
        name: String,
        description: String,
        isPublic: Boolean,
        songIds: List<String>,
    ): Result<Unit> = runCatching {
        if (name.isBlank()) {
            throw IllegalArgumentException("Çalma listesi adı boş olamaz.")
        }
        val response = playlistApiService.createPlaylist(CreatePlaylistRequestDto(name, description))
        val playlistId = response.data.id
        
        songIds.forEach { songId ->
            playlistApiService.addTrackToPlaylist(playlistId, AddTrackRequestDto(songId))
        }
    }

    override suspend fun getMyPlaylists(): Result<List<PlaylistSummaryModel>> = runCatching {
        playlistApiService.getMyPlaylists().data.map { dto ->
            val (startColor, endColor) = ArtworkPalette.colorPairForId(dto.id)
            PlaylistSummaryModel(
                id = dto.id,
                title = dto.name,
                description = dto.description.orEmpty(),
                coverStartColor = startColor,
                coverEndColor = endColor
            )
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit> = runCatching {
        playlistApiService.removeTrackFromPlaylist(playlistId, songId)
    }

    override suspend fun getPlaylistDetail(playlistId: String): Result<PlaylistDetailModel> = runCatching {
        playlistApiService.getPlaylistDetail(playlistId).data.toPlaylistDetailModel()
    }

    override suspend fun renamePlaylist(playlistId: String, newName: String): Result<Unit> = runCatching {
        if (newName.isBlank()) {
            throw IllegalArgumentException("Çalma listesi adı boş olamaz.")
        }
        playlistApiService.renamePlaylist(playlistId, RenamePlaylistRequestDto(newName))
    }

    private fun PlaylistWithSongsDto.toPlaylistDetailModel(): PlaylistDetailModel {
        val (startColor, endColor) = ArtworkPalette.colorPairForId(id)
        return PlaylistDetailModel(
            id = id,
            title = name,
            description = description.orEmpty(),
            creator = PLAYLIST_SOURCE_LABEL,
            songCount = songs.size,
            totalDuration = songs.sumOf { it.durationMs }.toTotalDurationLabel(),
            coverStartColor = startColor,
            coverEndColor = endColor,
            songs = songs.mapIndexed { index, song ->
                song.toSongItem(isPlaying = index == 0)
            },
        )
    }

    private fun SongDto.toSongItem(isPlaying: Boolean): SongItem {
        val (startColor, endColor) = ArtworkPalette.colorPairForId(id)
        return SongItem(
            id = id,
            title = title,
            artist = artist,
            duration = durationMs.toDurationLabel(),
            coverStartColor = startColor,
            coverEndColor = endColor,
            isLiked = false,
            isPlaying = isPlaying,
        )
    }

    private fun Long.toDurationLabel(): String {
        val totalSeconds = (this / MILLIS_IN_SECOND).coerceAtLeast(0)
        val minutes = totalSeconds / SECONDS_IN_MINUTE
        val seconds = totalSeconds % SECONDS_IN_MINUTE
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    private fun Long.toTotalDurationLabel(): String {
        val totalMinutes = (this / MILLIS_IN_MINUTE).coerceAtLeast(0)
        val hours = totalMinutes / MINUTES_IN_HOUR
        val minutes = totalMinutes % MINUTES_IN_HOUR
        return if (hours > 0) {
            "$hours sa $minutes dk"
        } else {
            "$minutes dk"
        }
    }

    private companion object {
        const val SONG_LIMIT = 50
        const val PLAYLIST_SOURCE_LABEL = "LyraApp"
        const val MILLIS_IN_SECOND = 1_000L
        const val SECONDS_IN_MINUTE = 60L
        const val MILLIS_IN_MINUTE = 60_000L
        const val MINUTES_IN_HOUR = 60L
    }
}
