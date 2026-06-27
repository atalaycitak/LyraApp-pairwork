package com.turkcell.lyraapp.ui.premium.plans

import com.turkcell.lyraapp.data.membership.MembershipPlan

data class PremiumPlansUiState(
    val isLoading: Boolean = true,
    val plans: List<MembershipPlan> = emptyList(),
    val selectedPlanId: String? = null,
    val isContinueEnabled: Boolean = false
)

sealed interface PremiumPlansIntent {
    data object LoadPlans : PremiumPlansIntent
    data class SelectPlan(val planId: String) : PremiumPlansIntent
    data object OnContinueClick : PremiumPlansIntent
    data object OnBackClick : PremiumPlansIntent
}

sealed interface PremiumPlansEffect {
    data class NavigateToPayment(val planId: String) : PremiumPlansEffect
    data class ShowError(val message: String) : PremiumPlansEffect
    data object NavigateBack : PremiumPlansEffect
}
