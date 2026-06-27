package com.turkcell.lyraapp.ui.premium.payment

import com.turkcell.lyraapp.data.membership.MembershipPlan

data class PaymentUiState(
    val isLoadingPlan: Boolean = true,
    val plan: MembershipPlan? = null,
    val cardNumber: String = "",
    val cardName: String = "",
    val cardExpiry: String = "",
    val cardCvc: String = "",
    val isProcessingPayment: Boolean = false,
    val isPayButtonEnabled: Boolean = false
)

sealed interface PaymentIntent {
    data class LoadPlan(val planId: String) : PaymentIntent
    data class CardNumberChanged(val number: String) : PaymentIntent
    data class CardNameChanged(val name: String) : PaymentIntent
    data class CardExpiryChanged(val expiry: String) : PaymentIntent
    data class CardCvcChanged(val cvc: String) : PaymentIntent
    data object OnPayClick : PaymentIntent
    data object OnBackClick : PaymentIntent
}

sealed interface PaymentEffect {
    data class NavigateBackToHome(val success: Boolean) : PaymentEffect
    data class ShowError(val message: String) : PaymentEffect
    data object NavigateBack : PaymentEffect
}
