package com.turkcell.lyraapp.ui.premium.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.membership.MembershipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumPlansViewModel @Inject constructor(
    private val membershipRepository: MembershipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumPlansUiState())
    val uiState: StateFlow<PremiumPlansUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PremiumPlansEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(PremiumPlansIntent.LoadPlans)
    }

    fun onIntent(intent: PremiumPlansIntent) {
        when (intent) {
            is PremiumPlansIntent.LoadPlans -> loadPlans()
            is PremiumPlansIntent.SelectPlan -> selectPlan(intent.planId)
            is PremiumPlansIntent.OnContinueClick -> handleContinueClick()
            is PremiumPlansIntent.OnBackClick -> navigateBack()
        }
    }

    private fun loadPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = membershipRepository.getPlans()
            result.onSuccess { plans ->
                // Select the first one by default if available
                val defaultSelected = plans.firstOrNull()?.id
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        plans = plans,
                        selectedPlanId = defaultSelected,
                        isContinueEnabled = defaultSelected != null
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _effect.send(PremiumPlansEffect.ShowError(error.message ?: "Planlar yüklenirken bir hata oluştu."))
            }
        }
    }

    private fun selectPlan(planId: String) {
        _uiState.update { state ->
            state.copy(
                selectedPlanId = planId,
                isContinueEnabled = true
            )
        }
    }

    private fun handleContinueClick() {
        val selectedId = _uiState.value.selectedPlanId
        if (selectedId != null) {
            viewModelScope.launch {
                _effect.send(PremiumPlansEffect.NavigateToPayment(selectedId))
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(PremiumPlansEffect.NavigateBack)
        }
    }
}
