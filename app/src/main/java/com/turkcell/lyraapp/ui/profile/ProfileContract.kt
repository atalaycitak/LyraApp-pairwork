package com.turkcell.lyraapp.ui.profile

import com.turkcell.lyraapp.data.profile.UserProfile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profileInfo: UserProfile? = null,
    val isDarkMode: Boolean = false
)

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data class OnThemeToggle(val isDarkMode: Boolean) : ProfileIntent
    data object OnSettingsClick : ProfileIntent
    data object OnAudioQualityClick : ProfileIntent
    data object OnOfflineDownloadClick : ProfileIntent
    data object OnNotificationsClick : ProfileIntent
    data object OnPrivacyClick : ProfileIntent
    data object OnHelpClick : ProfileIntent
}

sealed interface ProfileEffect {
    data object NavigateToNotifications : ProfileEffect
    data class ShowSnackbar(val message: String) : ProfileEffect
}
