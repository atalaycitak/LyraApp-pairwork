package com.turkcell.lyraapp.data.profile

/**
 * Profil ekranında gösterilecek kullanıcı bilgilerini temsil eden model.
 */
data class UserProfile(
    val id: String,
    val name: String,
    val username: String,
    val initials: String,
    val isPremium: Boolean,
    val playlistCount: Int,
    val followersCount: String,
    val followingCount: Int
)
