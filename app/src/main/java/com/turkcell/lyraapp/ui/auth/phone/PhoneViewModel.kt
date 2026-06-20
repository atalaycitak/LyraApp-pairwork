package com.turkcell.lyraapp.ui.auth.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneUiState())
    val uiState: StateFlow<PhoneUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PhoneEffect>(Channel.BUFFERED)
    val effect: Flow<PhoneEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: PhoneIntent) {
        when (intent) {
            is PhoneIntent.PhoneNumberChanged -> updatePhoneNumber(intent.value)
            is PhoneIntent.Submit -> submit()
        }
    }

    private fun updatePhoneNumber(value: String) {
        // Strip non-digit characters to check length, but allow UI to have spaces if needed
        val digits = value.filter { it.isDigit() }
        _uiState.update { 
            it.copy(
                phoneNumber = value,
                isSubmitEnabled = digits.length >= 10 // Basic validation (5xx xxx xxxx)
            )
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Backend expects +90 prefix, ensure it's there
            var phone = state.phoneNumber.filter { it.isDigit() }
            if (phone.startsWith("90")) {
                phone = "+$phone"
            } else if (phone.startsWith("0")) {
                phone = "+9$phone"
            } else {
                phone = "+90$phone"
            }

            val result = authRepository.requestOtp(phone)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { 
                    _effect.send(PhoneEffect.NavigateToOtp(phone))
                }
                .onFailure { error ->
                    _effect.send(PhoneEffect.ShowError(error.message ?: "İşlem başarısız."))
                }
        }
    }
}
