package com.turkcell.lyraapp.ui.auth.otp

data class OtpUiState(
    val phoneNumber: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val isSubmitEnabled: Boolean = false
)

sealed interface OtpIntent {
    data class CodeChanged(val code: String) : OtpIntent
    data object Submit : OtpIntent
    data object ResendCode : OtpIntent
}

sealed interface OtpEffect {
    data object NavigateToHome : OtpEffect
    data object NavigateToCompleteProfile : OtpEffect
    data class ShowError(val message: String) : OtpEffect
    data class ShowMessage(val message: String) : OtpEffect
}
