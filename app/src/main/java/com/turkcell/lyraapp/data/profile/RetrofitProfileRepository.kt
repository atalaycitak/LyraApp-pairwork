package com.turkcell.lyraapp.data.profile

import com.turkcell.lyraapp.data.playlist.PlaylistApiService
import javax.inject.Inject

class RetrofitProfileRepository @Inject constructor(
    private val profileApiService: ProfileApiService,
    private val playlistApiService: PlaylistApiService
) : ProfileRepository {

    override suspend fun getProfileInfo(): Result<UserProfile> = runCatching {
        val response = profileApiService.getMe()
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("Profil bilgileri alınamadı: ${response.code()}")
        }
        val userDto = response.body()!!.data
        val playlistCount = getPlaylistCount()
        mapToUserProfile(userDto, playlistCount)
    }

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        birthDate: String
    ): Result<Unit> = runCatching {
        val request = UpdateProfileRequest(firstName, lastName, birthDate)
        val response = profileApiService.updateProfile(request)
        if (!response.isSuccessful) {
            throw Exception("Profil güncellenemedi: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun recordPlay(songId: String): Result<Boolean> = runCatching {
        val request = RecordPlayRequestDto(songId)
        val response = profileApiService.recordPlay(request)
        if (response.isSuccessful) {
            response.body()?.data?.recorded ?: false
        } else {
            false
        }
    }

    private suspend fun getPlaylistCount(): Int =
        runCatching {
            playlistApiService.getMyPlaylists().data.size
        }.getOrDefault(0)

    private fun mapToUserProfile(
        userDto: com.turkcell.lyraapp.data.auth.UserDto,
        playlistCount: Int
    ): UserProfile {
        val calculatedName = if (!userDto.firstName.isNullOrBlank() || !userDto.lastName.isNullOrBlank()) {
            listOfNotNull(userDto.firstName, userDto.lastName).joinToString(" ")
        } else if (!userDto.displayName.isNullOrBlank()) {
            userDto.displayName
        } else {
            "İsimsiz Kullanıcı"
        }

        val calculatedInitials = calculatedName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

        return UserProfile(
            id = userDto.id,
            phone = userDto.phone,
            firstName = userDto.firstName,
            lastName = userDto.lastName,
            birthDate = userDto.birthDate,
            name = calculatedName,
            username = userDto.phone.replace("+", ""),
            initials = calculatedInitials,
            membership = userDto.membership,
            playlistCount = playlistCount,
            followersCount = "1.2B", // Dummy — API'da yok
            followingCount = 348 // Dummy — API'da yok
        )
    }
}
