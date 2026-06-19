package com.turkcell.lyraapp.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.notifications.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _effect = Channel<NotificationsEffect>(Channel.BUFFERED)
    val effect: Flow<NotificationsEffect> = _effect.receiveAsFlow()

    init {
        loadPreferences()
    }

    fun onIntent(intent: NotificationsIntent) {
        when (intent) {
            NotificationsIntent.BackClicked -> sendEffect(NotificationsEffect.NavigateBack)
            NotificationsIntent.Retry -> loadPreferences()
            is NotificationsIntent.PreferenceToggled -> updatePreference(
                preferenceId = intent.preferenceId,
                isEnabled = intent.isEnabled,
            )
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            notificationsRepository.getNotificationsFeed()
                .onSuccess { feed ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            preferences = feed.preferences,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        NotificationsEffect.ShowError(
                            error.message ?: "Bildirim ayarları yüklenemedi.",
                        ),
                    )
                }
        }
    }

    private fun updatePreference(preferenceId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            notificationsRepository.updatePreference(preferenceId, isEnabled)
                .onSuccess { updatedPreference ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            preferences = currentState.preferences.map { preference ->
                                if (preference.id == updatedPreference.id) {
                                    updatedPreference
                                } else {
                                    preference
                                }
                            },
                        )
                    }
                    _effect.send(NotificationsEffect.ShowMessage("Bildirim ayarı güncellendi."))
                }
                .onFailure { error ->
                    _effect.send(
                        NotificationsEffect.ShowError(
                            error.message ?: "Bildirim ayarı güncellenemedi.",
                        ),
                    )
                }
        }
    }

    private fun sendEffect(effect: NotificationsEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
