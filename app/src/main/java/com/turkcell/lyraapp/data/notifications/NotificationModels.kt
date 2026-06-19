package com.turkcell.lyraapp.data.notifications

/**
 * Bildirim ayarları ekranında gösterilecek tercih listesini temsil eder.
 */
data class NotificationsFeed(
    val preferences: List<NotificationPreference>,
)

data class NotificationPreference(
    val id: String,
    val title: String,
    val description: String,
    val type: NotificationPreferenceType,
    val isEnabled: Boolean,
)

enum class NotificationPreferenceType {
    General,
    NewMusic,
    Recommendations,
    PlaylistUpdates,
    Downloads,
    ListeningReminders,
}
