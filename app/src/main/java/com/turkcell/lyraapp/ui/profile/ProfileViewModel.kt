package com.turkcell.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.profile.ProfileRepository
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
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileEffect> = _effect.receiveAsFlow()

    init {
        onIntent(ProfileIntent.LoadProfile)
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.OnThemeToggle -> {
                _uiState.update { it.copy(isDarkMode = intent.isDarkMode) }
            }
            ProfileIntent.OnSettingsClick -> sendEffect(ProfileEffect.ShowSnackbar("Ayarlar tıklandı"))
            ProfileIntent.OnAudioQualityClick -> sendEffect(ProfileEffect.ShowSnackbar("Ses kalitesi seçenekleri"))
            ProfileIntent.OnOfflineDownloadClick -> sendEffect(ProfileEffect.ShowSnackbar("Çevrimdışı indirme ayarları"))
            ProfileIntent.OnNotificationsClick -> sendEffect(ProfileEffect.ShowSnackbar("Bildirim ayarları"))
            ProfileIntent.OnPrivacyClick -> sendEffect(ProfileEffect.ShowSnackbar("Gizlilik ayarları"))
            ProfileIntent.OnHelpClick -> sendEffect(ProfileEffect.ShowSnackbar("Yardım ve destek"))
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = profileRepository.getProfileInfo()
            result.fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profileInfo = profile
                        )
                    }
                },
                onFailure = { error ->
                    val msg = error.message ?: "Profil bilgisi alınamadı"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = msg
                        )
                    }
                    sendEffect(ProfileEffect.ShowSnackbar(msg))
                }
            )
        }
    }

    private fun sendEffect(effect: ProfileEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
