package com.turkcell.lyraapp.ui.profile

import com.turkcell.lyraapp.data.profile.MockProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var repository: MockProfileRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MockProfileRepository()
        viewModel = ProfileViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile happy path fetches profile successfully`() = runTest {
        // Given (init bloğunda LoadProfile tetiklendiği için ekstra intent'e gerek yok)
        
        // When
        advanceUntilIdle() // Coroutine'lerin tamamlanmasını bekle

        // Then
        val state = viewModel.uiState.value
        assertFalse("Yükleme durumu false olmalı", state.isLoading)
        assertNotNull("ProfileInfo boş olmamalı", state.profileInfo)
        assertEquals("Atalay Çıtak", state.profileInfo?.name)
        assertEquals("AÇ", state.profileInfo?.initials)
        assertEquals(127, state.profileInfo?.playlistCount)
        assertTrue("Premium olmalı", state.profileInfo?.isPremium == true)
    }

    @Test
    fun `onThemeToggle updates theme state correctly`() = runTest {
        // When
        viewModel.onIntent(ProfileIntent.OnThemeToggle(isDarkMode = true))
        
        // Then
        val state = viewModel.uiState.value
        assertTrue("Tema koyu olmalı", state.isDarkMode)
    }
}
