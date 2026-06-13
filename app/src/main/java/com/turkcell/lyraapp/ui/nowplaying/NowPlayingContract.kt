package com.turkcell.lyraapp.ui.nowplaying

/**
 * Now Playing ekranının MVI sözleşmesi: UiState + Intent + Effect (bkz. mvi-contracts.md).
 *
 * Oynatma durumu (play/pause, ilerleme) bu iterasyonda yerel state ile yönetilir;
 * gerçek medya oynatıcı (MediaSession/ExoPlayer) entegrasyonu ayrı iterasyonda ele alınacaktır.
 */
data class NowPlayingUiState(
    val isLoading: Boolean = false,
    val trackTitle: String = "",
    val artistName: String = "",
    val playlistName: String = "",
    val artworkStartColor: Long = 0xFF000000,
    val artworkEndColor: Long = 0xFF000000,
    val durationMs: Long = 0L,
    val currentPositionMs: Long = 0L,
    val isPlaying: Boolean = true,
    val isFavorite: Boolean = false,
    val isShuffleOn: Boolean = false,
    val isRepeatOn: Boolean = false,
)

/**
 * Kullanıcıdan gelen niyetler. UI yalnızca bu tipleri yayımlar; iş mantığını çalıştırmaz.
 */
sealed interface NowPlayingIntent {
    /** Oynat/Duraklat butonuna tıklandı. */
    data object TogglePlayPause : NowPlayingIntent

    /** Favori kalp ikonuna tıklandı. */
    data object ToggleFavorite : NowPlayingIntent

    /** Karıştır butonuna tıklandı. */
    data object ToggleShuffle : NowPlayingIntent

    /** Tekrarla butonuna tıklandı. */
    data object ToggleRepeat : NowPlayingIntent

    /** Önceki parça butonuna tıklandı. */
    data object SkipPrevious : NowPlayingIntent

    /** Sonraki parça butonuna tıklandı. */
    data object SkipNext : NowPlayingIntent

    /** İlerleme çubuğu sürüklendiğinde yeni konum bildirilir. */
    data class SeekTo(val positionMs: Long) : NowPlayingIntent

    /** Geri (chevron down) butonuna tıklandı. */
    data object NavigateBack : NowPlayingIntent

    /** Besleme yüklemesi başarısız olduğunda kullanıcı yeniden dener. */
    data object Retry : NowPlayingIntent
}

/**
 * Tek seferlik (one-shot) olaylar: navigasyon, snackbar vb. State içinde tutulmaz,
 * böylece konfigürasyon değişiminde tekrar tetiklenmez.
 */
sealed interface NowPlayingEffect {
    /** Geri navigasyonu tetiklenir. */
    data object NavigateBack : NowPlayingEffect

    data class ShowError(val message: String) : NowPlayingEffect
}
