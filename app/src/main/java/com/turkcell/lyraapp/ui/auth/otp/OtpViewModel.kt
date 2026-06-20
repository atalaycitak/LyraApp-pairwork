package com.turkcell.lyraapp.ui.auth.otp

import androidx.lifecycle.SavedStateHandle
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
class OtpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val phoneNumber: String = checkNotNull(savedStateHandle["phone"])

    private val _uiState = MutableStateFlow(OtpUiState(phoneNumber = phoneNumber))
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private val _effect = Channel<OtpEffect>(Channel.BUFFERED)
    val effect: Flow<OtpEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: OtpIntent) {
        when (intent) {
            is OtpIntent.CodeChanged -> updateCode(intent.code)
            is OtpIntent.Submit -> submit()
            is OtpIntent.ResendCode -> resendCode()
        }
    }

    private fun updateCode(code: String) {
        if (code.length <= 6) {
            val digitsOnly = code.filter { it.isDigit() }
            _uiState.update {
                it.copy(
                    code = digitsOnly,
                    isSubmitEnabled = digitsOnly.length == 6
                )
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.verifyOtp(state.phoneNumber, state.code)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { firstTime ->
                    if (firstTime) {
                        _effect.send(OtpEffect.NavigateToCompleteProfile)
                    } else {
                        _effect.send(OtpEffect.NavigateToHome)
                    }
                }
                .onFailure { error ->
                    _effect.send(OtpEffect.ShowError(error.message ?: "Doğrulama başarısız."))
                }
        }
    }

    private fun resendCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.requestOtp(phoneNumber)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess {
                    _effect.send(OtpEffect.ShowMessage("Kod tekrar gönderildi."))
                }
                .onFailure { error ->
                    _effect.send(OtpEffect.ShowError(error.message ?: "İşlem başarısız."))
                }
        }
    }
}
