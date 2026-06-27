package com.turkcell.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.profile.ProfileRepository
import com.turkcell.lyraapp.data.theme.ThemePreferences
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
    private val profileRepository: ProfileRepository,
    private val themePreferences: ThemePreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileEffect> = _effect.receiveAsFlow()

    init {
        onIntent(ProfileIntent.LoadProfile)
        
        viewModelScope.launch {
            themePreferences.isDarkModeFlow.collect { isDark ->
                // Null ise varsayılan olarak cihaz temasını bilmediğimizden uiState'i 
                // güncellemeyebiliriz veya true/false olarak bırakabiliriz, ancak null işlenmiyor.
                // İdealde burada null durumu için sistem teması alınmalı. Şimdilik UI sadece toggle yapabiliyor.
                if (isDark != null) {
                    _uiState.update { it.copy(isDarkMode = isDark) }
                }
            }
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.OnThemeToggle -> {
                themePreferences.setDarkMode(intent.isDarkMode)
            }
            ProfileIntent.OnSettingsClick -> sendEffect(ProfileEffect.ShowSnackbar("Ayarlar tıklandı"))
            ProfileIntent.OnAudioQualityClick -> sendEffect(ProfileEffect.ShowSnackbar("Ses kalitesi seçenekleri"))
            ProfileIntent.OnOfflineDownloadClick -> sendEffect(ProfileEffect.ShowSnackbar("Çevrimdışı indirme ayarları"))
            ProfileIntent.OnNotificationsClick -> sendEffect(ProfileEffect.NavigateToNotifications)
            ProfileIntent.OnPrivacyClick -> sendEffect(ProfileEffect.ShowSnackbar("Gizlilik ayarları"))
            ProfileIntent.OnHelpClick -> sendEffect(ProfileEffect.ShowSnackbar("Yardım ve destek"))
            ProfileIntent.OnPremiumBannerClick -> sendEffect(ProfileEffect.NavigateToPremiumPlans)
            
            ProfileIntent.OnEditProfileClick -> {
                _uiState.update { it.copy(showEditDialog = true) }
            }
            ProfileIntent.OnDismissEditDialog -> {
                _uiState.update { it.copy(showEditDialog = false) }
            }
            is ProfileIntent.OnSaveProfile -> saveProfile(intent.firstName, intent.lastName, intent.birthDate)
            ProfileIntent.OnLogoutClick -> {
                viewModelScope.launch {
                    authRepository.logout()
                    sendEffect(ProfileEffect.NavigateToLogin)
                }
            }
        }
    }

    private fun saveProfile(firstName: String, lastName: String, birthDate: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = profileRepository.updateProfile(firstName, lastName, birthDate)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, showEditDialog = false) }
                    sendEffect(ProfileEffect.ShowSnackbar("Profil güncellendi"))
                    loadProfile() // Yeniden yükle
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    sendEffect(ProfileEffect.ShowSnackbar(error.message ?: "Profil güncellenemedi"))
                }
            )
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = profileRepository.getProfileInfo()
            result.fold(
                onSuccess = { profile ->
                    _uiState.update { it.copy(isLoading = false, profileInfo = profile) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    sendEffect(ProfileEffect.ShowSnackbar(error.message ?: "Profil bilgisi alınamadı"))
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
