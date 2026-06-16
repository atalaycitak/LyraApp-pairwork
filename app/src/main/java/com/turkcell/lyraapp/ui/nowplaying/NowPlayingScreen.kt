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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
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
            }
        }
    }

    NowPlayingScreen(
        state = uiState,
        player = viewModel.player,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Now Playing ("Simdi Caliyor") ekrani.
 *
 * Tamamen durumsuzdur (stateless): durumu [state] uzerinden alir, kullanici
 * etkilesimlerini [onIntent] ile yukari yayimlar. Ekran goruntusune birebir uygun
 * olarak su bolumleri icerir:
 * - Ust bar (chevron + baslik + menu)
 * - Album kapagi (gradyan)
 * - Sarki bilgisi + favori
 * - Ilerleme cubugu
 * - Oynatma kontrolleri
 * - Alt arac cubugu
 */
@Composable
fun NowPlayingScreen(
    state: NowPlayingUiState,
    player: Player?,
    onIntent: (NowPlayingIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopBar(
                playlistName = state.playlistName,
                onBackClick = { onIntent(NowPlayingIntent.NavigateBack) },
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                this.player = player
                            }
                        },
                        update = { view ->
                            view.player = player
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/** Ust bar: asagi ok (chevron) + "SIMDI CALIYOR" + playlist adi + uc nokta menu. */
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
                text = "SIMDI CALIYOR",
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
        IconButton(onClick = { /* Menu aksiyonu henuz tanimli degil */ }) {
            Icon(
                imageVector = LyraIcons.MoreVert,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// Custom components removed for default ExoPlayer UI

// ── Preview ──

private val previewState = NowPlayingUiState(
    trackTitle = "Neon Sokaklar",
    artistName = "Sehir Isiklari",
    playlistName = "Gece Vardiyasi",
    artworkStartColor = 0xFFD98E4A,
    artworkEndColor = 0xFF8A5526,
    durationMs = 223_000L,
    currentPositionMs = 93_000L,
    isPlaying = true,
    isFavorite = true,
    isShuffleOn = true,
    isRepeatOn = true,
)

@Preview(name = "NowPlaying - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun NowPlayingScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        NowPlayingScreen(state = previewState, player = null, onIntent = {})
    }
}

@Preview(name = "NowPlaying - Light", showBackground = true, showSystemUi = true)
@Composable
private fun NowPlayingScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        NowPlayingScreen(state = previewState, player = null, onIntent = {})
    }
}
