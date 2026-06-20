package com.turkcell.lyraapp.ui.home

import com.turkcell.lyraapp.data.home.ForYouSong
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
    val forYou: List<ForYouSong> = emptyList(),
)

sealed interface HomeIntent {
    /** Besleme yüklemesi başarısız olduğunda kullanıcı yeniden dener. */
    data object Retry : HomeIntent

    /** Kullanıcı bir şarkı kartına (QuickPick, RecentlyPlayed veya ForYou) tıkladı. */
    data class SongClicked(val songId: String) : HomeIntent
}

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect

    /** NowPlaying ekranına geçiş tetiklenir; [songId] navArgument olarak iletilir. */
    data class NavigateToNowPlaying(val songId: String) : HomeEffect

    /** PlaylistDetail ekranına geçiş tetiklenir; [playlistId] navArgument olarak iletilir. */
    data class NavigateToPlaylistDetail(val playlistId: String) : HomeEffect
}

