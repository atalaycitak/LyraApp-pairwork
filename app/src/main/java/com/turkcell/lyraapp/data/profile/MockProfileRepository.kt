package com.turkcell.lyraapp.data.profile

import com.turkcell.lyraapp.data.membership.MembershipDto
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
                phone = "+905551234567",
                firstName = "Zeynep",
                lastName = "Kaya",
                birthDate = "1998-05-12",
                name = "Zeynep Kaya",
                username = "zeynepk",
                initials = "ZK",
                membership = MembershipDto(
                    planId = "one-time",
                    type = "one-time",
                    status = "active",
                    autoRenew = false,
                    startedAt = "2026-06-01T00:00:00Z",
                    expiresAt = "2026-07-01T00:00:00Z"
                ),
                playlistCount = 127,
                followersCount = "1.2B",
                followingCount = 348
            )
        )
    }

    override suspend fun updateProfile(firstName: String, lastName: String, birthDate: String): Result<Unit> {
        delay(500)
        return Result.success(Unit)
    }

    override suspend fun recordPlay(songId: String): Result<Boolean> {
        delay(100)
        return Result.success(true)
    }
}
