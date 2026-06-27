package com.turkcell.lyraapp.ui.favorites

import com.turkcell.lyraapp.data.favorites.MockFavoritesRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var repository: MockFavoritesRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MockFavoritesRepository()
        viewModel = FavoritesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads favorites feed successfully`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Yukleme durumu false olmali", state.isLoading)
        assertEquals("filter-all", state.selectedFilterId)
        assertEquals(4, state.filters.size)
        assertEquals(6, state.items.size)
    }

    @Test
    fun `filter selected filters items by type`() = runTest {
        advanceUntilIdle()

        viewModel.onIntent(FavoritesIntent.FilterSelected("filter-songs"))

        val state = viewModel.uiState.value
        assertEquals("filter-songs", state.selectedFilterId)
        assertEquals(2, state.items.size)
        assertTrue(state.items.all { it.type == com.turkcell.lyraapp.data.favorites.FavoriteItemType.Song })
    }

    @Test
    fun `filter all shows all items`() = runTest {
        advanceUntilIdle()

        viewModel.onIntent(FavoritesIntent.FilterSelected("filter-playlists"))
        assertEquals(2, viewModel.uiState.value.items.size)

        viewModel.onIntent(FavoritesIntent.FilterSelected("filter-all"))
        assertEquals(6, viewModel.uiState.value.items.size)
    }

    @Test
    fun `favorite clicked removes item from list`() = runTest {
        advanceUntilIdle()

        val initialCount = viewModel.uiState.value.items.size
        viewModel.onIntent(FavoritesIntent.FavoriteClicked("favorite-1"))

        val state = viewModel.uiState.value
        assertEquals(initialCount - 1, state.items.size)
    }

    @Test
    fun `item clicked for song emits navigate to player effect`() = runTest {
        advanceUntilIdle()

        val effect = backgroundScope.async(testDispatcher) {
            viewModel.effect.first()
        }
        advanceUntilIdle()

        viewModel.onIntent(FavoritesIntent.ItemClicked("favorite-1"))
        advanceUntilIdle()

        assertTrue(effect.await() is FavoritesEffect.NavigateToPlayer)
    }
}
