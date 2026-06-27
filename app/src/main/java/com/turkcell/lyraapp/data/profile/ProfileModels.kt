package com.turkcell.lyraapp.data.profile

import com.turkcell.lyraapp.data.membership.MembershipDto

/**
 * Profil ekranında gösterilecek kullanıcı bilgilerini temsil eden model.
 */
data class UserProfile(
    val id: String,
    val phone: String,
    val firstName: String?,
    val lastName: String?,
    val birthDate: String?,
    val name: String,
    val username: String,
    val initials: String,
    val membership: MembershipDto?,
    val playlistCount: Int,
    val followersCount: String,
    val followingCount: Int
) {
    /** Aktif premium uyeligi olup olmadigini doner. */
    val isPremium: Boolean get() = membership?.status == "active"
}

