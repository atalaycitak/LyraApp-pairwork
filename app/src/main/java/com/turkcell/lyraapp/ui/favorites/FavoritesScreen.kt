package com.turkcell.lyraapp.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.favorites.FavoriteFilter
import com.turkcell.lyraapp.data.favorites.FavoriteItem
import com.turkcell.lyraapp.data.favorites.FavoriteItemType
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Favorites akisinin durumlu (stateful) giris noktasi.
 */
@Composable
fun FavoritesRoute(
    onNavigateToPlayer: (songId: String) -> Unit = {},
    onNavigateToPlaylistDetail: (playlistId: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FavoritesEffect.NavigateToPlayer -> onNavigateToPlayer(effect.songId)
                is FavoritesEffect.NavigateToPlaylistDetail -> onNavigateToPlaylistDetail(effect.playlistId)
                is FavoritesEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is FavoritesEffect.ShowError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Tekrar dene",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(FavoritesIntent.Retry)
                    }
                }
            }
        }
    }

    FavoritesScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Favoriler ekrani.
 *
 * Tamamen durumsuzdur: state'i parametre olarak alir, kullanici etkilesimlerini intent olarak
 * yukari yayar.
 */
@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onIntent: (FavoritesIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (state.isLoading && state.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
            ) {
                item { FavoritesHeader(count = state.items.size) }
                item {
                    FilterChipsRow(
                        filters = state.filters,
                        selectedFilterId = state.selectedFilterId,
                        onFilterSelected = { onIntent(FavoritesIntent.FilterSelected(it)) },
                    )
                }
                if (state.items.isEmpty()) {
                    item { EmptyFavorites() }
                } else {
                    items(state.items, key = { it.id }) { item ->
                        FavoriteItemRow(
                            item = item,
                            onClick = { onIntent(FavoritesIntent.ItemClicked(item.id)) },
                            onFavoriteClick = { onIntent(FavoritesIntent.FavoriteClicked(item.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesHeader(count: Int) {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LyraIcons.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Favoriler",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "$count kayıtlı içerik",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "API'daki şarkılardan türetilen favori listen burada görünür.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FilterChipsRow(
    filters: List<FavoriteFilter>,
    selectedFilterId: String,
    onFilterSelected: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filters, key = { it.id }) { filter ->
            val isSelected = filter.id == selectedFilterId
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter.id) },
                label = {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = LyraIcons.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    null
                },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    enabled = true,
                    selected = isSelected,
                ),
            )
        }
    }
}

@Composable
private fun FavoriteItemRow(
    item: FavoriteItem,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 12.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(
            startColor = item.artworkStartColor,
            endColor = item.artworkEndColor,
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${item.type.label()} - ${item.subtitle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = item.durationLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (item.isDownloaded) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = LyraIcons.Check,
                contentDescription = "İndirildi",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = LyraIcons.Favorite,
                contentDescription = "Favorilerden kaldır",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EmptyFavorites() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = LyraIcons.FavoriteOutlined,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(42.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Bu filtrede favori yok",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Beğenilerini değiştirdikçe bu liste otomatik güncellenir.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Artwork(
    startColor: Long,
    endColor: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Brush.linearGradient(listOf(Color(startColor), Color(endColor))))
            .background(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                ),
            ),
    )
}

private fun FavoriteItemType.label(): String =
    when (this) {
        FavoriteItemType.Song -> "Şarkı"
        FavoriteItemType.Album -> "Albüm"
        FavoriteItemType.Playlist -> "Liste"
    }

private val previewState = FavoritesUiState(
    selectedFilterId = FavoritesUiState.DEFAULT_FILTER_ID,
    filters = listOf(
        FavoriteFilter("filter-all", "Tümü", null),
        FavoriteFilter("filter-songs", "Şarkılar", FavoriteItemType.Song),
    ),
    items = listOf(
        FavoriteItem("s_neon-tide", "Neon Tide", "Aurora Drift", FavoriteItemType.Song, 0xFF8B6FB8, 0xFF4A3D6B, "3:42", false),
        FavoriteItem("s_midnight-road", "Midnight Road", "City Lights", FavoriteItemType.Song, 0xFF6FBF5A, 0xFF356B2A, "4:08", false),
        FavoriteItem("s_sunset-loop", "Sunset Loop", "Mira", FavoriteItemType.Song, 0xFF7C83D9, 0xFF3E4486, "2:58", false),
    ),
)

@Preview(name = "Favorites - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun FavoritesScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        FavoritesScreen(state = previewState, onIntent = {})
    }
}

@Preview(name = "Favorites - Light", showBackground = true, showSystemUi = true)
@Composable
private fun FavoritesScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        FavoritesScreen(state = previewState, onIntent = {})
    }
}
