package com.turkcell.lyraapp.data.playlist

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [PlaylistRepository] arayuzunun sahte implementasyonu.
 */
class MockPlaylistRepository @Inject constructor() : PlaylistRepository {

    private val mockPlaylistDetails = mutableMapOf<String, PlaylistDetailModel>()

    override suspend fun getAvailableSongs(): Result<List<SelectableSong>> {
        delay(500)
        return Result.success(
            listOf(
                SelectableSong("s1", "Gece Yarısı", "Mavi Deniz", 0xFF8BC34A, 0xFF33691E),
                SelectableSong("s2", "Sessiz Şehir", "Ela Tuna", 0xFF9575CD, 0xFF4527A0),
                SelectableSong("s3", "Yıldız Tozu", "Polaris", 0xFF4DD0E1, 0xFF006064),
                SelectableSong("s4", "Sahil Yolu", "Kumsal", 0xFFE57373, 0xFFB71C1C),
                SelectableSong("s5", "Mor Bulutlar", "Derin Kaya", 0xFF4DB6AC, 0xFF004D40),
                SelectableSong("s6", "İlk Işık", "Sabah Ezgisi", 0xFF64B5F6, 0xFF0D47A1),
                SelectableSong("s7", "Kayıp Anlar", "Eko", 0xFF4FC3F7, 0xFF01579B),
            )
        )
    }

    override suspend fun createPlaylist(
        name: String,
        description: String,
        isPublic: Boolean,
        songIds: List<String>
    ): Result<Unit> {
        delay(1000)
        return if (name.isBlank()) {
            Result.failure(IllegalArgumentException("Calma listesi adi bos olamaz."))
        } else {
            val playlistId = "mock_${name.hashCode()}"
            mockPlaylistDetails[playlistId] = createDefaultPlaylistDetail(playlistId).copy(
                title = name,
                description = description
            )
            Result.success(Unit)
        }
    }

    override suspend fun renamePlaylist(playlistId: String, newName: String): Result<Unit> = runCatching {
        if (newName.isBlank()) throw IllegalArgumentException("Isim bos olamaz")
        val current = mockPlaylistDetails.getOrPut(playlistId) {
            createDefaultPlaylistDetail(playlistId)
        }
        mockPlaylistDetails[playlistId] = current.copy(title = newName)
    }

    override suspend fun getPlaylistDetail(playlistId: String): Result<PlaylistDetailModel> {
        delay(500)
        val detail = mockPlaylistDetails.getOrPut(playlistId) {
            createDefaultPlaylistDetail(playlistId)
        }
        return Result.success(detail)
    }

    override suspend fun getMyPlaylists(): Result<List<PlaylistSummaryModel>> =
        Result.success(
            mockPlaylistDetails.values.map { detail ->
                PlaylistSummaryModel(
                    id = detail.id,
                    title = detail.title,
                    description = detail.description,
                    coverStartColor = detail.coverStartColor,
                    coverEndColor = detail.coverEndColor
                )
            }
        )

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit> = runCatching {
        val current = mockPlaylistDetails.getOrPut(playlistId) {
            createDefaultPlaylistDetail(playlistId)
        }
        val updatedSongs = current.songs.filterNot { it.id == songId }
        mockPlaylistDetails[playlistId] = current.copy(
            songs = updatedSongs,
            songCount = updatedSongs.size
        )
    }

    private fun createDefaultPlaylistDetail(playlistId: String): PlaylistDetailModel =
        PlaylistDetailModel(
            id = playlistId,
            title = "Gece Sürüşü",
            description = "Karanlık yollar için synth-pop",
            creator = "Zeynep Kaya",
            songCount = 6,
            totalDuration = "23 dk",
            coverStartColor = 0xFFB39DDB,
            coverEndColor = 0xFF7E57C2,
            songs = listOf(
                SongItem("1", "Neon Sokaklar", "Şehir Işıkları", "3:43", 0xFF8D6E63, 0xFF5D4037, isLiked = true, isPlaying = true),
                SongItem("2", "Gece Yarısı", "Mavi Deniz", "3:34", 0xFF8BC34A, 0xFF33691E, isLiked = true),
                SongItem("3", "Mor Bulutlar", "Derin Kaya", "3:52", 0xFF4DB6AC, 0xFF004D40),
                SongItem("4", "Son Tren", "Peron", "3:37", 0xFF4DB6AC, 0xFF004D40),
                SongItem("5", "Yıldız Tozu", "Polaris", "4:07", 0xFF4DD0E1, 0xFF006064, isLiked = true),
                SongItem("6", "Sahil Yolu", "Kumsal", "4:15", 0xFFE57373, 0xFFB71C1C)
            )
        )
}
