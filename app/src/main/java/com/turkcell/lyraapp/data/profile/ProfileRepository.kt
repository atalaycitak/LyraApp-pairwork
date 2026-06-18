package com.turkcell.lyraapp.data.profile

/**
 * Profil verilerine erişimi sağlayan Repository arayüzü.
 */
interface ProfileRepository {
    suspend fun getProfileInfo(): Result<UserProfile>
}
