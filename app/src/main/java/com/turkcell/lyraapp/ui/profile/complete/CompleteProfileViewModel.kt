package com.turkcell.lyraapp.ui.profile.complete

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
class CompleteProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompleteProfileUiState())
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CompleteProfileEffect>(Channel.BUFFERED)
    val effect: Flow<CompleteProfileEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: CompleteProfileIntent) {
        when (intent) {
            is CompleteProfileIntent.FirstNameChanged -> updateForm { it.copy(firstName = intent.value) }
            is CompleteProfileIntent.LastNameChanged -> updateForm { it.copy(lastName = intent.value) }
            is CompleteProfileIntent.DayChanged -> {
                val value = intent.value.filter { it.isDigit() }.take(2)
                updateForm { it.copy(day = value) }
            }
            is CompleteProfileIntent.MonthChanged -> {
                val value = intent.value.filter { it.isDigit() }.take(2)
                updateForm { it.copy(month = value) }
            }
            is CompleteProfileIntent.YearChanged -> {
                val value = intent.value.filter { it.isDigit() }.take(4)
                updateForm { it.copy(year = value) }
            }
            is CompleteProfileIntent.Submit -> submit()
        }
    }

    private fun updateForm(transform: (CompleteProfileUiState) -> CompleteProfileUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isSubmitEnabled = updated.isFormValid())
        }
    }

    private fun CompleteProfileUiState.isFormValid(): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                day.length == 2 &&
                month.length == 2 &&
                year.length == 4
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // API expects YYYY-MM-DD
            val formattedDate = "${state.year}-${state.month}-${state.day}"
            
            val result = authRepository.completeProfile(
                firstName = state.firstName,
                lastName = state.lastName,
                birthDate = formattedDate
            )
            
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess {
                    _effect.send(CompleteProfileEffect.NavigateToHome)
                }
                .onFailure { error ->
                    _effect.send(CompleteProfileEffect.ShowError(error.message ?: "İşlem başarısız."))
                }
        }
    }
}
