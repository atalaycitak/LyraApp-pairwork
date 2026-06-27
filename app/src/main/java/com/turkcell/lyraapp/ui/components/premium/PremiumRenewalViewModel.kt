package com.turkcell.lyraapp.ui.components.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.membership.MembershipPlan
import com.turkcell.lyraapp.data.membership.MembershipRepository
import com.turkcell.lyraapp.data.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PremiumRenewalUiState(
    val showDialog: Boolean = false,
    val daysLeft: Long = 0L,
    val monthlyPlan: MembershipPlan? = null,
    val oneTimePlan: MembershipPlan? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class PremiumRenewalViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val membershipRepository: MembershipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumRenewalUiState())
    val uiState: StateFlow<PremiumRenewalUiState> = _uiState.asStateFlow()

    private var hasChecked = false

    fun checkRenewalIfNeeded() {
        if (hasChecked) return
        hasChecked = true

        viewModelScope.launch {
            val profileResult = profileRepository.getProfileInfo()
            if (profileResult.isSuccess) {
                val profile = profileResult.getOrThrow()
                val membership = profile.membership

                if (profile.isPremium && membership?.expiresAt != null) {
                    try {
                        val expiresInstant = java.time.Instant.parse(membership.expiresAt)
                        val now = java.time.Instant.now()
                        val diff = java.time.Duration.between(now, expiresInstant).toDays()
                        val daysLeft = diff.coerceAtLeast(0)

                        if (daysLeft <= 3) {
                            fetchPlansAndShowDialog(daysLeft)
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        }
    }

    private suspend fun fetchPlansAndShowDialog(daysLeft: Long) {
        val plansResult = membershipRepository.getPlans()
        if (plansResult.isSuccess) {
            val plans = plansResult.getOrThrow()
            val monthly = plans.find { it.type == "recurring" }
            val oneTime = plans.find { it.type == "one-time" }
            
            _uiState.update {
                it.copy(
                    showDialog = true,
                    daysLeft = daysLeft,
                    monthlyPlan = monthly,
                    oneTimePlan = oneTime,
                    isLoading = false
                )
            }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showDialog = false) }
    }
}
