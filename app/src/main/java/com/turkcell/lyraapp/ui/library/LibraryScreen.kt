package com.turkcell.lyraapp.ui.library

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.library.LibraryFilter
import com.turkcell.lyraapp.data.library.LibraryItem
import com.turkcell.lyraapp.data.library.LibraryItemType
import com.turkcell.lyraapp.data.library.LibraryQuickAction
import com.turkcell.lyraapp.data.library.LibraryQuickActionType
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Kutuphane akisinin durumlu (stateful) giris noktasi.
 */
@Composable
fun LibraryRoute(
    onNavigateToCreatePlaylist: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LibraryEffect.NavigateToCreatePlaylist -> onNavigateToCreatePlaylist()
                is LibraryEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is LibraryEffect.ShowError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Tekrar dene",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(LibraryIntent.Retry)
                    }
                }
            }
        }
    }

    LibraryScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Kutuphane ekrani.
 *
 * Tamamen durumsuzdur: state'i parametre olarak alir, kullanici etkilesimlerini intent olarak
 * yukari yayar. Alt cubuk boslugu dis Scaffold'dan gelir; burada status bar boslugu yonetilir.
 */
@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onIntent: (LibraryIntent) -> Unit,
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
                item { LibraryHeader() }
                item {
                    QuickActionsRow(
                        actions = state.quickActions,
                        onActionClick = { onIntent(LibraryIntent.QuickActionClicked(it)) },
                    )
                }
                item {
                    FilterChipsRow(
                        filters = state.filters,
                        selectedFilterId = state.selectedFilterId,
                        onFilterSelected = { onIntent(LibraryIntent.FilterSelected(it)) },
                    )
                }
                item { LibrarySectionHeader(count = state.items.size) }
                items(state.items, key = { it.id }) { item ->
                    LibraryItemRow(
                        item = item,
                        onClick = { onIntent(LibraryIntent.ItemClicked(item.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryHeader() {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp),
    ) {
        Text(
            text = "Kutuphane",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Kaydettigin muzikleri, listeleri ve cevrimdisi icerikleri yonet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuickActionsRow(
    actions: List<LibraryQuickAction>,
    onActionClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(actions, key = { it.id }) { action ->
            QuickActionCard(
                action = action,
                onClick = { onActionClick(action.id) },
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    action: LibraryQuickAction,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBubble(icon = action.type.icon())
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = action.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun IconBubble(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun FilterChipsRow(
    filters: List<LibraryFilter>,
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
private fun LibrarySectionHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Kayitli icerikler",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$count oge",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LibraryItemRow(
    item: LibraryItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 2.dp),
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
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (item.isDownloaded) {
            Icon(
                imageVector = LyraIcons.Check,
                contentDescription = "Indirildi",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            imageVector = LyraIcons.MoreVert,
            contentDescription = "Daha fazla",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
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

private fun LibraryQuickActionType.icon(): ImageVector =
    when (this) {
        LibraryQuickActionType.CreatePlaylist -> LyraIcons.QueueMusic
        LibraryQuickActionType.Downloads -> LyraIcons.LibraryMusic
        LibraryQuickActionType.LikedSongs -> LyraIcons.Favorite
    }

private val previewState = LibraryUiState(
    selectedFilterId = LibraryUiState.DEFAULT_FILTER_ID,
    filters = listOf(
        LibraryFilter("filter-all", "Tumu", null),
        LibraryFilter("filter-playlists", "Calma listeleri", LibraryItemType.Playlist),
        LibraryFilter("filter-albums", "Albumler", LibraryItemType.Album),
        LibraryFilter("filter-artists", "Sanatcilar", LibraryItemType.Artist),
    ),
    quickActions = listOf(
        LibraryQuickAction(
            "action-create-playlist",
            "Yeni calma listesi",
            "Kapak, ad ve sarkilari belirle",
            LibraryQuickActionType.CreatePlaylist,
        ),
        LibraryQuickAction(
            "action-downloads",
            "Indirilenler",
            "Cevrimdisi dinlemeye hazir",
            LibraryQuickActionType.Downloads,
        ),
    ),
    items = listOf(
        LibraryItem("library-1", "Gece Surusu", "Calma listesi - 42 sarki", LibraryItemType.Playlist, 0xFF8B6FB8, 0xFF4A3D6B, true),
        LibraryItem("library-2", "Derin Mavi", "Album - Okyanus", LibraryItemType.Album, 0xFF6FBF5A, 0xFF356B2A, true),
        LibraryItem("library-3", "Polaris", "Sanatci - 128 bin dinleyici", LibraryItemType.Artist, 0xFF3D5A80, 0xFF1B2A45, false),
    ),
)

@Preview(name = "Library - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun LibraryScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        LibraryScreen(state = previewState, onIntent = {})
    }
}

@Preview(name = "Library - Light", showBackground = true, showSystemUi = true)
@Composable
private fun LibraryScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        LibraryScreen(state = previewState, onIntent = {})
    }
}
