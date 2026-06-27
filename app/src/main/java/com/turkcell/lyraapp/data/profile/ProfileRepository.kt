package com.turkcell.lyraapp.data.profile

/**
 * Profil verilerine erişimi sağlayan Repository arayüzü.
 */
interface ProfileRepository {
    suspend fun getProfileInfo(): Result<UserProfile>
    suspend fun updateProfile(firstName: String, lastName: String, birthDate: String): Result<Unit>
    suspend fun recordPlay(songId: String): Result<Boolean>
}
