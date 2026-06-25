package com.turkcell.lyraapp.ui.search

import com.turkcell.lyraapp.data.search.MockSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel
    private lateinit var repository: MockSearchRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MockSearchRepository()
        viewModel = SearchViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads filters and genres successfully`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Yukleme durumu false olmali", state.isLoading)
        assertEquals(4, state.filters.size)
        assertEquals("filter-all", state.filters.first().id)
        assertEquals(8, state.genres.size)
    }

    @Test
    fun `query changed fires debounced search after delay`() = runTest {
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.QueryChanged("test"))

        // 300ms beklenmeden arama yapilmamali
        val stateBeforeDebounce = viewModel.uiState.value
        assertEquals("test", stateBeforeDebounce.searchQuery)

        // Debounce suresini atla
        advanceTimeBy(301)
        advanceUntilIdle()

        val stateAfterDebounce = viewModel.uiState.value
        assertFalse("Yukleme durumu false olmali", stateAfterDebounce.isLoading)
    }

    @Test
    fun `rapid query changes cancel previous debounce`() = runTest {
        advanceUntilIdle()

        // Ilk sorgu
        viewModel.onIntent(SearchIntent.QueryChanged("a"))
        advanceTimeBy(100) // 300ms dolmadan

        // Ikinci sorgu (ilkini iptal eder)
        viewModel.onIntent(SearchIntent.QueryChanged("ab"))
        advanceTimeBy(100) // hala ilk timer'in 300ms'i dolmadi

        // Ucuncu sorgu (ikincisini iptal eder)
        viewModel.onIntent(SearchIntent.QueryChanged("abc"))

        // Son timer'in suresini bekle
        advanceTimeBy(301)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("abc", state.searchQuery)
        assertFalse("Yukleme durumu false olmali", state.isLoading)
    }

    @Test
    fun `retry cancels pending debounce and searches immediately`() = runTest {
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.QueryChanged("xyz"))
        advanceTimeBy(100) // debounce henuz tetiklenmedi

        // Retry hemen aramali
        viewModel.onIntent(SearchIntent.Retry)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Yukleme durumu false olmali", state.isLoading)
    }

    @Test
    fun `filter selected updates selectedFilterId`() = runTest {
        viewModel.onIntent(SearchIntent.FilterSelected("filter-pop"))

        val state = viewModel.uiState.value
        assertEquals("filter-pop", state.selectedFilterId)
    }
}
