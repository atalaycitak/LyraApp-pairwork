package com.turkcell.lyraapp.data.profile

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Profil ekranı için statik sahte (mock) veri döndüren repository implementasyonu.
 */
class MockProfileRepository @Inject constructor() : ProfileRepository {
    override suspend fun getProfileInfo(): Result<UserProfile> {
        delay(800) // Ağ gecikmesi simülasyonu
        return Result.success(
            UserProfile(
                id = "usr_123",
                name = "Atalay Çıtak",
                username = "atalaycitak",
                initials = "AÇ",
                isPremium = true,
                playlistCount = 127,
                followersCount = "1.2B",
                followingCount = 348
            )
        )
    }
}
