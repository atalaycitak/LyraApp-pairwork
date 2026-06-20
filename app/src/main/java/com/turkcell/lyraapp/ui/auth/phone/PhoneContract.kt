package com.turkcell.lyraapp.ui.auth.phone

data class PhoneUiState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isSubmitEnabled: Boolean = false
)

sealed interface PhoneIntent {
    data class PhoneNumberChanged(val value: String) : PhoneIntent
    data object Submit : PhoneIntent
}

sealed interface PhoneEffect {
    data class NavigateToOtp(val phoneNumber: String) : PhoneEffect
    data class ShowError(val message: String) : PhoneEffect
}
