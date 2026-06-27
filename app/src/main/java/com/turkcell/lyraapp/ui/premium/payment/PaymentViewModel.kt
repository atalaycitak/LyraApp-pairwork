package com.turkcell.lyraapp.ui.premium.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.membership.CardDto
import com.turkcell.lyraapp.data.membership.CheckoutRequestDto
import com.turkcell.lyraapp.data.membership.MembershipRepository
import com.turkcell.lyraapp.data.profile.ProfileRepository
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
class PaymentViewModel @Inject constructor(
    private val membershipRepository: MembershipRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PaymentEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.LoadPlan -> loadPlan(intent.planId)
            is PaymentIntent.CardNumberChanged -> updateCardNumber(intent.number)
            is PaymentIntent.CardNameChanged -> updateCardName(intent.name)
            is PaymentIntent.CardExpiryChanged -> updateCardExpiry(intent.expiry)
            is PaymentIntent.CardCvcChanged -> updateCardCvc(intent.cvc)
            is PaymentIntent.OnPayClick -> processPayment()
            is PaymentIntent.OnBackClick -> navigateBack()
        }
    }

    private fun loadPlan(planId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPlan = true) }
            val result = membershipRepository.getPlans()
            result.onSuccess { plans ->
                val selectedPlan = plans.find { it.id == planId }
                if (selectedPlan != null) {
                    _uiState.update { it.copy(isLoadingPlan = false, plan = selectedPlan) }
                } else {
                    _uiState.update { it.copy(isLoadingPlan = false) }
                    _effect.send(PaymentEffect.ShowError("Plan bulunamadı."))
                    _effect.send(PaymentEffect.NavigateBack)
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingPlan = false) }
                _effect.send(PaymentEffect.ShowError(error.message ?: "Hata oluştu."))
            }
        }
    }

    private fun updateCardNumber(number: String) {
        // Sadece rakamları tutalım ve 16 haneye kısıtlayalım (boşlukları sonradan UI'da koyarız veya viewModelda)
        val digits = number.filter { it.isDigit() }.take(16)
        _uiState.update { it.copy(cardNumber = digits) }
        checkFormValidity()
    }

    private fun updateCardName(name: String) {
        _uiState.update { it.copy(cardName = name.take(50)) }
        checkFormValidity()
    }

    private fun updateCardExpiry(expiry: String) {
        val digits = expiry.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(cardExpiry = digits) }
        checkFormValidity()
    }

    private fun updateCardCvc(cvc: String) {
        val digits = cvc.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(cardCvc = digits) }
        checkFormValidity()
    }

    private fun checkFormValidity() {
        val s = _uiState.value
        val isValid = s.cardNumber.length in 15..16 &&
                s.cardName.isNotBlank() &&
                s.cardExpiry.length == 4 &&
                s.cardCvc.length >= 3
        _uiState.update { it.copy(isPayButtonEnabled = isValid) }
    }

    private fun processPayment() {
        val state = _uiState.value
        val plan = state.plan ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true) }

            // Expiry parsing MM/YY
            val month = state.cardExpiry.take(2).toIntOrNull() ?: 1
            var year = state.cardExpiry.drop(2).take(2).toIntOrNull() ?: 0
            if (year < 100) year += 2000 // 25 -> 2025

            val card = CardDto(
                number = state.cardNumber,
                expMonth = month,
                expYear = year,
                cvc = state.cardCvc,
                holderName = state.cardName
            )

            val result = membershipRepository.checkout(plan.type, card)
            result.onSuccess { checkoutData ->
                _uiState.update { it.copy(isProcessingPayment = false) }
                // Profil verilerini guncelle
                profileRepository.getProfileInfo() 
                _effect.send(PaymentEffect.NavigateBackToHome(true))
            }.onFailure { error ->
                _uiState.update { it.copy(isProcessingPayment = false) }
                _effect.send(PaymentEffect.ShowError(error.message ?: "Ödeme reddedildi."))
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(PaymentEffect.NavigateBack)
        }
    }
}
