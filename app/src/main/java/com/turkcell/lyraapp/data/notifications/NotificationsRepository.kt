package com.turkcell.lyraapp.data.notifications

/**
 * Bildirim ayarlarının veri kaynağı soyutlaması.
 *
 * API'da bildirim endpoint'i bulunmadığı için bu iterasyonda mock/local implementasyon kullanılır.
 */
interface NotificationsRepository {
    suspend fun getNotificationsFeed(): Result<NotificationsFeed>

    suspend fun updatePreference(preferenceId: String, isEnabled: Boolean): Result<NotificationPreference>
}
