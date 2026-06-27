package com.turkcell.lyraapp.data.profile

import com.turkcell.lyraapp.data.auth.UserDto
import com.turkcell.lyraapp.data.playlist.AddTrackRequestDto
import com.turkcell.lyraapp.data.playlist.CreatePlaylistRequestDto
import com.turkcell.lyraapp.data.playlist.PlaylistApiService
import com.turkcell.lyraapp.data.playlist.PlaylistDetailResponseDto
import com.turkcell.lyraapp.data.playlist.PlaylistDto
import com.turkcell.lyraapp.data.playlist.PlaylistResponseDto
import com.turkcell.lyraapp.data.playlist.PlaylistsResponseDto
import com.turkcell.lyraapp.data.playlist.RenamePlaylistRequestDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class RetrofitProfileRepositoryTest {

    @Test
    fun `getProfileInfo maps authenticated playlist count`() = runTest {
        val repository = RetrofitProfileRepository(
            profileApiService = FakeProfileApiService(),
            playlistApiService = FakePlaylistApiService(
                playlists = listOf(
                    playlistDto("playlist-1"),
                    playlistDto("playlist-2"),
                    playlistDto("playlist-3")
                )
            )
        )

        val profile = repository.getProfileInfo().getOrThrow()

        assertEquals(3, profile.playlistCount)
    }

    @Test
    fun `getProfileInfo keeps profile available when playlist count fails`() = runTest {
        val repository = RetrofitProfileRepository(
            profileApiService = FakeProfileApiService(),
            playlistApiService = FakePlaylistApiService(shouldFail = true)
        )

        val profile = repository.getProfileInfo().getOrThrow()

        assertEquals("Zeynep Kaya", profile.name)
        assertEquals(0, profile.playlistCount)
    }

    private class FakeProfileApiService : ProfileApiService {
        override suspend fun getMe(): Response<UserResponseDto> =
            Response.success(UserResponseDto(defaultUser()))

        override suspend fun updateProfile(request: UpdateProfileRequest): Response<UserResponseDto> =
            Response.success(UserResponseDto(defaultUser()))

        override suspend fun recordPlay(request: RecordPlayRequestDto): Response<RecordPlayResponseDto> =
            Response.success(RecordPlayResponseDto(RecordPlayData(recorded = true)))
    }

    private class FakePlaylistApiService(
        private val playlists: List<PlaylistDto> = emptyList(),
        private val shouldFail: Boolean = false
    ) : PlaylistApiService {

        override suspend fun getPlaylists(): PlaylistsResponseDto =
            PlaylistsResponseDto(emptyList())

        override suspend fun getPlaylistDetail(id: String): PlaylistDetailResponseDto =
            throw NotImplementedError("Unused in this test")

        override suspend fun getMyPlaylists(): PlaylistsResponseDto {
            if (shouldFail) {
                throw IllegalStateException("Playlist count failed")
            }
            return PlaylistsResponseDto(playlists)
        }

        override suspend fun createPlaylist(request: CreatePlaylistRequestDto): PlaylistResponseDto =
            PlaylistResponseDto(playlistDto("created-playlist"))

        override suspend fun addTrackToPlaylist(playlistId: String, request: AddTrackRequestDto) = Unit

        override suspend fun removeTrackFromPlaylist(playlistId: String, songId: String) = Unit

        override suspend fun renamePlaylist(playlistId: String, request: RenamePlaylistRequestDto) = Unit
    }

    private companion object {
        fun defaultUser() = UserDto(
            id = "user-1",
            phone = "+905551112233",
            displayName = null,
            firstName = "Zeynep",
            lastName = "Kaya",
            birthDate = "2000-01-01",
            createdAt = "2026-01-01T00:00:00Z",
            profileCompleted = true,
            membership = null
        )

        fun playlistDto(id: String) = PlaylistDto(
            id = id,
            name = "Playlist $id",
            description = null,
            createdAt = "2026-01-01T00:00:00Z"
        )
    }
}
