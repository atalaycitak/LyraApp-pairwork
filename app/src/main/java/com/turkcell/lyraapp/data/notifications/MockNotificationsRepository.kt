package com.turkcell.lyraapp.data.notifications

import javax.inject.Inject
import javax.inject.Singleton

/**
 * API sözleşmesinde bildirim endpoint'i olmadığı için kullanılan local mock repository.
 */
@Singleton
class MockNotificationsRepository @Inject constructor() : NotificationsRepository {

    private var preferences = defaultPreferences()

    override suspend fun getNotificationsFeed(): Result<NotificationsFeed> =
        Result.success(NotificationsFeed(preferences = preferences))

    override suspend fun updatePreference(
        preferenceId: String,
        isEnabled: Boolean,
    ): Result<NotificationPreference> {
        val currentPreference = preferences.firstOrNull { it.id == preferenceId }
            ?: return Result.failure(IllegalArgumentException("Bildirim ayarı bulunamadı."))

        val updatedPreference = currentPreference.copy(isEnabled = isEnabled)
        preferences = preferences.map { preference ->
            if (preference.id == preferenceId) updatedPreference else preference
        }
        return Result.success(updatedPreference)
    }

    private fun defaultPreferences(): List<NotificationPreference> = listOf(
        NotificationPreference(
            id = "general",
            title = "Genel bildirimler",
            description = "LyraApp bildirimlerini açıp kapatır.",
            type = NotificationPreferenceType.General,
            isEnabled = true,
        ),
        NotificationPreference(
            id = "new-music",
            title = "Yeni çıkan şarkılar",
            description = "Sevdiğin sanatçıların yeni şarkıları için haber verilir.",
            type = NotificationPreferenceType.NewMusic,
            isEnabled = true,
        ),
        NotificationPreference(
            id = "recommendations",
            title = "Öneriler",
            description = "Dinleme alışkanlıklarına göre müzik önerileri gönderilir.",
            type = NotificationPreferenceType.Recommendations,
            isEnabled = true,
        ),
        NotificationPreference(
            id = "playlist-updates",
            title = "Çalma listesi güncellemeleri",
            description = "Takip ettiğin çalma listelerine eklenen şarkılar bildirilir.",
            type = NotificationPreferenceType.PlaylistUpdates,
            isEnabled = false,
        ),
        NotificationPreference(
            id = "downloads",
            title = "İndirme bildirimleri",
            description = "İndirme tamamlandığında veya hata oluştuğunda bildirim gösterilir.",
            type = NotificationPreferenceType.Downloads,
            isEnabled = true,
        ),
        NotificationPreference(
            id = "listening-reminders",
            title = "Dinleme hatırlatmaları",
            description = "Günün seçili zamanlarında müzik dinleme hatırlatması yapılır.",
            type = NotificationPreferenceType.ListeningReminders,
            isEnabled = false,
        ),
    )
}
