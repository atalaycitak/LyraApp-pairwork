package com.turkcell.lyraapp.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Now Playing akışının durumlu (stateful) giriş noktası.
 *
 * [NowPlayingViewModel]'i Hilt'ten alır, durumu yaşam döngüsüne duyarlı şekilde toplar ve
 * tek seferlik [NowPlayingEffect]'leri tüketir. Geri navigasyonu [onNavigateBack] lambda'sıyla
 * dışarıya köprülenir (ViewModel navigasyon API'si bilmez).
 */
@Composable
fun NowPlayingRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NowPlayingEffect.NavigateBack -> onNavigateBack()
                is NowPlayingEffect.ShowError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Tekrar dene",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(NowPlayingIntent.Retry)
                    }
                }
                is NowPlayingEffect.ShowDownloadResult -> {
                    snackbarHostState.showSnackbar(message = effect.message)
                }
            }
        }
    }

    NowPlayingScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Now Playing ("Şimdi Çalıyor") ekranı.
 *
 * Tamamen durumsuzdur (stateless): durumu [state] üzerinden alır, kullanıcı
 * etkileşimlerini [onIntent] ile yukarı yayımlar. Ekran görüntüsüne birebir uygun
 * olarak şu bölümleri içerir:
 * - Üst bar (chevron + başlık + menü)
 * - Albüm kapağı (gradyan)
 * - Şarkı bilgisi + favori
 * - İlerleme çubuğu
 * - Oynatma kontrolleri
 * - Alt araç çubuğu
 */
@Composable
fun NowPlayingScreen(
    state: NowPlayingUiState,
    onIntent: (NowPlayingIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (state.isLoading && state.trackTitle.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopBar(
                    playlistName = state.playlistName,
                    onBackClick = { onIntent(NowPlayingIntent.NavigateBack) },
                )

                Spacer(Modifier.height(24.dp))

                AlbumArtwork(
                    startColor = state.artworkStartColor,
                    endColor = state.artworkEndColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp)),
                )

                Spacer(Modifier.height(28.dp))

                TrackInfo(
                    title = state.trackTitle,
                    artist = state.artistName,
                    isFavorite = state.isFavorite,
                    isDownloaded = state.isDownloaded,
                    downloadProgress = state.downloadProgress,
                    onFavoriteClick = { onIntent(NowPlayingIntent.ToggleFavorite) },
                    onDownloadClick = { onIntent(NowPlayingIntent.DownloadSong) }
                )

                Spacer(Modifier.height(16.dp))

                ProgressSection(
                    currentPositionMs = state.currentPositionMs,
                    durationMs = state.durationMs,
                    onSeek = { onIntent(NowPlayingIntent.SeekTo(it)) },
                )

                Spacer(Modifier.height(16.dp))

                PlaybackControls(
                    isPlaying = state.isPlaying,
                    isShuffleOn = state.isShuffleOn,
                    isRepeatOn = state.isRepeatOn,
                    onTogglePlayPause = { onIntent(NowPlayingIntent.TogglePlayPause) },
                    onSkipPrevious = { onIntent(NowPlayingIntent.SkipPrevious) },
                    onSkipNext = { onIntent(NowPlayingIntent.SkipNext) },
                    onToggleShuffle = { onIntent(NowPlayingIntent.ToggleShuffle) },
                    onToggleRepeat = { onIntent(NowPlayingIntent.ToggleRepeat) },
                )

                Spacer(Modifier.weight(1f))

                BottomToolbar()

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/** Üst bar: aşağı ok (chevron) + "ŞİMDİ ÇALIYOR" + playlist adı + üç nokta menü. */
@Composable
private fun TopBar(
    playlistName: String,
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = LyraIcons.KeyboardArrowDown,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "ŞİMDİ ÇALIYOR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.5.sp,
            )
            Text(
                text = playlistName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = { /* Menü aksiyonu henüz tanımlı değil */ }) {
            Icon(
                imageVector = LyraIcons.MoreVert,
                contentDescription = "Menü",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Albüm kapağı: gradyan arka plan + hafif radyal parlama + konsantrik daire deseni.
 *
 * Gerçek API görsel URL'si sağladığında bu composable görsel yükleyiciyle değiştirilir.
 */
@Composable
private fun AlbumArtwork(
    startColor: Long,
    endColor: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    listOf(Color(startColor), Color(endColor)),
                ),
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.14f),
                        Color.Transparent,
                    ),
                    radius = 600f,
                ),
            ),
    )
}

/** Şarkı başlığı + sanatçı adı + favori kalp ikonu. */
@Composable
private fun TrackInfo(
    title: String,
    artist: String,
    isFavorite: Boolean,
    isDownloaded: Boolean,
    downloadProgress: Float?,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (downloadProgress != null) {
            CircularProgressIndicator(
                progress = { downloadProgress },
                modifier = Modifier.padding(12.dp).size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
        } else {
            IconButton(onClick = onDownloadClick) {
                Icon(
                    imageVector = if (isDownloaded) LyraIcons.CheckCircle else LyraIcons.Download,
                    contentDescription = "İndir",
                    tint = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                contentDescription = "Favori",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/** İlerleme çubuğu + süre etiketleri. */
@Composable
private fun ProgressSection(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
) {
    val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = progress,
            onValueChange = { fraction ->
                onSeek((fraction * durationMs).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatDuration(currentPositionMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Oynatma kontrolleri: karıştır, önceki, oynat/duraklat, sonraki, tekrarla. */
@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    isShuffleOn: Boolean,
    isRepeatOn: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControlIcon(
            icon = LyraIcons.Shuffle,
            contentDescription = "Karıştır",
            tint = if (isShuffleOn) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onToggleShuffle,
        )
        ControlIcon(
            icon = LyraIcons.SkipPrevious,
            contentDescription = "Önceki",
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = onSkipPrevious,
            size = 36,
        )
        PlayPauseButton(
            isPlaying = isPlaying,
            onClick = onTogglePlayPause,
        )
        ControlIcon(
            icon = LyraIcons.SkipNext,
            contentDescription = "Sonraki",
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = onSkipNext,
            size = 36,
        )
        ControlIcon(
            icon = LyraIcons.Repeat,
            contentDescription = "Tekrarla",
            tint = if (isRepeatOn) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onToggleRepeat,
        )
    }
}

/** Büyük pembe oynat/duraklat butonu. */
@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isPlaying) LyraIcons.Pause else LyraIcons.PlayArrow,
            contentDescription = if (isPlaying) "Duraklat" else "Oynat",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(36.dp),
        )
    }
}

/** Tekil kontrol ikonu (karıştır, önceki, sonraki, tekrarla). */
@Composable
private fun ControlIcon(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    size: Int = 28,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size.dp),
        )
    }
}

/** Alt araç çubuğu: kuyruk, arka plan, şarkı sözleri ikonları. */
@Composable
private fun BottomToolbar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { /* Kuyruk aksiyonu henüz tanımlı değil */ }) {
            Icon(
                imageVector = LyraIcons.QueueMusic,
                contentDescription = "Kuyruk",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Arka plan aksiyonu henüz tanımlı değil */ },
                ),
        ) {
            Icon(
                imageVector = LyraIcons.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "Arka plan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = { /* Şarkı sözleri aksiyonu henüz tanımlı değil */ }) {
            Icon(
                imageVector = LyraIcons.Lyrics,
                contentDescription = "Şarkı Sözleri",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** Milisaniyeyi "m:ss" formatına çevirir. */
private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

// ── Preview ──

private val previewState = NowPlayingUiState(
    trackTitle = "Neon Sokaklar",
    artistName = "Şehir Işıkları",
    playlistName = "Gece Vardiyasi",
    artworkStartColor = 0xFFD98E4A,
    artworkEndColor = 0xFF8A5526,
    durationMs = 223_000L,
    currentPositionMs = 93_000L,
    isPlaying = true,
    isFavorite = true,
    isShuffleOn = true,
)

@Preview(name = "NowPlaying - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun NowPlayingScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        NowPlayingScreen(state = previewState, onIntent = {})
    }
}

@Preview(name = "NowPlaying - Light", showBackground = true, showSystemUi = true)
@Composable
private fun NowPlayingScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        NowPlayingScreen(state = previewState, onIntent = {})
    }
}
