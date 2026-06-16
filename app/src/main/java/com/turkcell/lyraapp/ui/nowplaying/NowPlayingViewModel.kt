package com.turkcell.lyraapp.ui.nowplaying

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.song.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

/**
 * Now Playing ekranının ViewModel'i (bkz. mvi-viewmodel-rules.md).
 *
 * [songId] navigasyon argümanından [SavedStateHandle] ile alınır. Yükleme adımları:
 * 1. [SongRepository.getSongById] → şarkı metadatasını State'e yazar.
 * 2. [SongRepository.getStreamUrl] → ExoPlayer'a imzalı URL beslenir ve oynatma başlar.
 *
 * ExoPlayer Context gerektirdiğinden [di/NowPlayingModule.kt] içinde ApplicationContext
 * kullanılarak Hilt tarafından sağlanır; ViewModel doğrudan Context tutmaz.
 *
 * ExoPlayer Singleton Güvenliği: loadTrack() her çağrıldığında player önce durdurulur ve
 * medya temizlenir; bu sayede farklı şarkılar arasında geçişte eski ses karışmaz.
 *
 * Progress Tracking: [Player.Listener.onPlaybackStateChanged] oynatma başladığında
 * [startPositionPolling] tetiklenir; coroutine döngüsü [POSITION_POLL_INTERVAL_MS] aralıkla
 * ExoPlayer'ın gerçek pozisyonunu okur. Simülasyon tamamen kaldırıldı.
 */
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val songRepository: SongRepository,
    val player: ExoPlayer,
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle["songId"])

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    private val _effect = Channel<NowPlayingEffect>(Channel.BUFFERED)
    val effect: Flow<NowPlayingEffect> = _effect.receiveAsFlow()

    /** Pozisyon yoklama coroutine'i; oynatma durduğunda iptal edilir. */
    private var positionPollingJob: Job? = null

    /**
     * ExoPlayer durumu değiştiğinde:
     * - STATE_READY → oynatma başladıysa [startPositionPolling] tetiklenir.
     * - STATE_ENDED → pozisyon yoklaması durdurulur, UI sıfırlanır.
     */
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    if (player.isPlaying) startPositionPolling()
                }
                Player.STATE_ENDED -> {
                    positionPollingJob?.cancel()
                    _uiState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
                }
                else -> Unit
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startPositionPolling()
            } else {
                positionPollingJob?.cancel()
            }
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }
    }

    init {
        player.addListener(playerListener)
        loadTrack()
    }

    fun onIntent(intent: NowPlayingIntent) {
        when (intent) {
            is NowPlayingIntent.TogglePlayPause -> togglePlayPause()
            is NowPlayingIntent.ToggleFavorite  -> { /* Favori API'si bu iterasyonda yok */ }
            is NowPlayingIntent.ToggleShuffle   -> _uiState.update { it.copy(isShuffleOn = !it.isShuffleOn) }
            is NowPlayingIntent.ToggleRepeat    -> _uiState.update { it.copy(isRepeatOn = !it.isRepeatOn) }
            is NowPlayingIntent.SkipPrevious    -> player.seekTo(0L)
            is NowPlayingIntent.SkipNext        -> { /* Kuyruk yönetimi bu iterasyonda yok */ }
            is NowPlayingIntent.SeekTo          -> player.seekTo(intent.positionMs)
            is NowPlayingIntent.NavigateBack    -> viewModelScope.launch { _effect.send(NowPlayingEffect.NavigateBack) }
            is NowPlayingIntent.Retry           -> loadTrack()
        }
    }

    private fun loadTrack() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // ExoPlayer singleton güvenliği: önceki şarkıyı durdur ve temizle.
            positionPollingJob?.cancel()
            player.stop()
            player.clearMediaItems()

            // Adım 1: Şarkı metadatasını çek
            val songResult = songRepository.getSongById(songId)
            songResult.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _effect.send(NowPlayingEffect.ShowError(error.message ?: "Şarkı bilgisi yüklenemedi."))
                return@launch
            }
            val song = songResult.getOrThrow()
            val (colorStart, colorEnd) = colorPairForId(song.id)
            _uiState.update {
                it.copy(
                    trackTitle = song.title,
                    artistName = song.artist,
                    playlistName = song.album ?: "",
                    artworkStartColor = colorStart,
                    artworkEndColor = colorEnd,
                    durationMs = song.durationMs,
                    currentPositionMs = 0L,
                )
            }

            // Adım 2: İmzalı stream URL'i çek ve ExoPlayer'a besle
            val urlResult = songRepository.getStreamUrl(songId)
            _uiState.update { it.copy(isLoading = false) }
            urlResult
                .onSuccess { streamData ->
                    val mediaItem = MediaItem.fromUri(streamData.url)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                    // isPlaying durumu ve pozisyon yoklaması playerListener üzerinden yönetilir.
                }
                .onFailure { error ->
                    _effect.send(NowPlayingEffect.ShowError(error.message ?: "Ses akışı başlatılamadı."))
                }
        }
    }

    private fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        // isPlaying State güncellemesi playerListener.onIsPlayingChanged üzerinden gelir.
    }

    /**
     * ExoPlayer'ın gerçek [currentPosition] değerini [POSITION_POLL_INTERVAL_MS] aralıkla
     * okuyarak [NowPlayingUiState.currentPositionMs]'i günceller.
     *
     * Önceki yoklama görevi varsa iptal edilerek yeni bir tane başlatılır;
     * böylece seek/restart gibi durumlarda çakışan iki döngü oluşmaz.
     */
    private fun startPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = viewModelScope.launch {
            while (true) {
                delay(POSITION_POLL_INTERVAL_MS)
                if (!player.isPlaying) break
                _uiState.update { it.copy(currentPositionMs = player.currentPosition) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        positionPollingJob?.cancel()
        player.removeListener(playerListener)
        player.release()
    }

    /**
     * Şarkı ID'sinden deterministik renk çifti türetir.
     * [RetrofitHomeRepository] ile aynı palet kullanılır; şarkı her zaman aynı rengi taşır.
     */
    private fun colorPairForId(id: String): Pair<Long, Long> {
        val index = abs(id.hashCode()) % ARTWORK_PALETTE.size
        return ARTWORK_PALETTE[index]
    }

    private companion object {
        const val POSITION_POLL_INTERVAL_MS = 500L

        val ARTWORK_PALETTE = listOf(
            Pair(0xFF8B6FB8L, 0xFF4A3D6BL),
            Pair(0xFF7C83D9L, 0xFF3E4486L),
            Pair(0xFFD98E4AL, 0xFF8A5526L),
            Pair(0xFF4AC2A8L, 0xFF1F6E5CL),
            Pair(0xFF6FBF5AL, 0xFF356B2AL),
            Pair(0xFF5AAFC9L, 0xFF2A5F73L),
            Pair(0xFF9B7FC4L, 0xFF5A4480L),
            Pair(0xFF6B5FB8L, 0xFF3A3270L),
            Pair(0xFF3FAE9CL, 0xFF1E5D52L),
            Pair(0xFFD9604AL, 0xFF8A3020L),
            Pair(0xFF4A8BD9L, 0xFF1E4580L),
            Pair(0xFFD9A84AL, 0xFF8A6020L),
        )
    }
}

