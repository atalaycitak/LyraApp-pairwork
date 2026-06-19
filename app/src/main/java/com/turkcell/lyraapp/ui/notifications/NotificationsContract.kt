package com.turkcell.lyraapp.ui.notifications

import com.turkcell.lyraapp.data.notifications.NotificationPreference

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val preferences: List<NotificationPreference> = emptyList(),
)

sealed interface NotificationsIntent {
    data object BackClicked : NotificationsIntent
    data object Retry : NotificationsIntent
    data class PreferenceToggled(
        val preferenceId: String,
        val isEnabled: Boolean,
    ) : NotificationsIntent
}

sealed interface NotificationsEffect {
    data object NavigateBack : NotificationsEffect
    data class ShowMessage(val message: String) : NotificationsEffect
    data class ShowError(val message: String) : NotificationsEffect
}
