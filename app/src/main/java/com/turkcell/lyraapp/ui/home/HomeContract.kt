package com.turkcell.lyraapp.ui.home

import com.turkcell.lyraapp.data.home.PlaylistForYou
import com.turkcell.lyraapp.data.home.QuickPick
import com.turkcell.lyraapp.data.home.RecentlyPlayed

/**
 * Home ekranının MVI sözleşmesi: UiState + Intent + Effect (bkz. mvi-contracts.md).
 *
 * Kart tıklamaları tek seferlik navigation effect'leriyle Route katmanına iletilir.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: String = "",
    val userInitials: String = "",
    val quickPicks: List<QuickPick> = emptyList(),
    val recentlyPlayed: List<RecentlyPlayed> = emptyList(),
    val playlistsForYou: List<PlaylistForYou> = emptyList(),
)

sealed interface HomeIntent {
    /** Besleme yüklemesi başarısız olduğunda kullanıcı yeniden dener. */
    data object Retry : HomeIntent

    /** Kullanıcı bir şarkı kartına (QuickPick veya RecentlyPlayed) tıkladı. */
    data class SongClicked(val songId: String) : HomeIntent

    /** Kullanıcı bir çalma listesi kartına tıkladı. */
    data class PlaylistClicked(val playlistId: String) : HomeIntent
}

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect

    /** NowPlaying ekranına geçiş tetiklenir; [songId] navArgument olarak iletilir. */
    data class NavigateToNowPlaying(val songId: String) : HomeEffect

    /** PlaylistDetail ekranına geçiş tetiklenir; [playlistId] navArgument olarak iletilir. */
    data class NavigateToPlaylistDetail(val playlistId: String) : HomeEffect
}

