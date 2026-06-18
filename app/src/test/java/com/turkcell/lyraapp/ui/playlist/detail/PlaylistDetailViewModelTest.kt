package com.turkcell.lyraapp.ui.playlist.detail

import com.turkcell.lyraapp.data.playlist.MockPlaylistRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistDetailViewModelTest {

    private lateinit var viewModel: PlaylistDetailViewModel
    private lateinit var repository: MockPlaylistRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MockPlaylistRepository()
        viewModel = PlaylistDetailViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPlaylist happy path fetches playlist detail successfully`() = runTest {
        // Given
        val playlistId = "test_playlist_123"

        // When
        viewModel.onIntent(PlaylistDetailIntent.LoadPlaylist(playlistId))
        
        // Coroutine'lerin tamamlanmasını bekle
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse("Yükleme durumu false olmalı", state.isLoading)
        assertNotNull("PlaylistDetail boş olmamalı", state.playlistDetail)
        assertEquals("Gece Sürüşü", state.playlistDetail?.title)
        assertEquals(6, state.playlistDetail?.songs?.size)
    }
}
