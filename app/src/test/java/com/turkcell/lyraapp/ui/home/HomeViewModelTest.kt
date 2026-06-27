package com.turkcell.lyraapp.ui.home

import com.turkcell.lyraapp.data.home.MockHomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
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
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: MockHomeRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MockHomeRepository()
        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads home feed successfully`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Yukleme durumu false olmali", state.isLoading)
        assertEquals("ZK", state.userInitials)
        assertEquals(6, state.quickPicks.size)
        assertEquals(3, state.recentlyPlayed.size)
        assertEquals(3, state.forYou.size)
    }

    @Test
    fun `greeting is set based on time of day`() = runTest {
        val state = viewModel.uiState.value
        assertNotNull(state.greeting)
        assertTrue(
            "Selamlama metni gecerli olmali: ${state.greeting}",
            state.greeting in listOf("Günaydın", "İyi günler", "İyi akşamlar")
        )
    }

    @Test
    fun `song clicked emits navigate to now playing effect`() = runTest {
        advanceUntilIdle()

        val effect = backgroundScope.async(testDispatcher) {
            viewModel.effect.first()
        }
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.SongClicked("qp-1"))
        advanceUntilIdle()

        assertEquals("qp-1", (effect.await() as HomeEffect.NavigateToNowPlaying).songId)
    }

    @Test
    fun `retry reloads feed`() = runTest {
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.Retry)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Yukleme durumu false olmali", state.isLoading)
        assertEquals(6, state.quickPicks.size)
    }
}
