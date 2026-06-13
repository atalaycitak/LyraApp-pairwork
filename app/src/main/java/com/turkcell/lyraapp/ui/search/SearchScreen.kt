package com.turkcell.lyraapp.ui.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.turkcell.lyraapp.ui.icons.LyraIcons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.search.Genre
import com.turkcell.lyraapp.data.search.SearchFilter
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Arama akışının durumlu (stateful) giriş noktası.
 *
 * [SearchViewModel]'i Hilt'ten alır, durumu yaşam döngüsüne duyarlı şekilde toplar ve
 * tek seferlik [SearchEffect]'leri tüketir. Yükleme hatasında snackbar üzerinden
 * "Tekrar dene" aksiyonu [SearchIntent.Retry] niyetine köprülenir.
 */
@Composable
fun SearchRoute(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SearchEffect.ShowError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Tekrar dene",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(SearchIntent.Retry)
                    }
                }
            }
        }
    }

    SearchScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Arama ("Ara") ekranı.
 *
 * Tamamen durumsuzdur (stateless): durumu [state] üzerinden alır, kullanıcı etkileşimlerini
 * [onIntent] ile yukarı yayımlar. Ekran görüntüsüne birebir uygun olarak şu bölümleri içerir:
 * - Başlık ("Ara")
 * - Arama alanı (TextField)
 * - Filtre çipleri (Hepsi, Pop, Elektronik, Akustik)
 * - "Türlere göz at" başlığı + 2 sütunlu tür kartları grid'i
 */
@Composable
fun SearchScreen(
    state: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (state.isLoading && state.genres.isEmpty()) {
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { SearchHeader() }
                item {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChanged = { onIntent(SearchIntent.QueryChanged(it)) },
                    )
                }
                item {
                    FilterChipsRow(
                        filters = state.filters,
                        selectedFilterId = state.selectedFilterId,
                        onFilterSelected = { onIntent(SearchIntent.FilterSelected(it)) },
                    )
                }
                item { BrowseGenresHeader() }
                item { GenreGrid(genres = state.genres) }
            }
        }
    }
}

/** "Ara" başlık metni. */
@Composable
private fun SearchHeader() {
    Text(
        text = "Ara",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp),
    )
}

/** Arama alanı: ikon + placeholder metinli TextField. */
@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                text = "Sarki, sanatci veya album",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = LyraIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    )
}

/** Yatay scrollable filtre çipleri satırı. */
@Composable
private fun FilterChipsRow(
    filters: List<SearchFilter>,
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

/** "Turlere goz at" bolum basligi. */
@Composable
private fun BrowseGenresHeader() {
    Text(
        text = "Turlere goz at",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 20.dp),
    )
}

/** Tur kartlarinin 2 sutunlu sabit grid'i (dikey scroll LazyColumn'a aittir). */
@Composable
private fun GenreGrid(genres: List<Genre>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        genres.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { genre ->
                    GenreCard(genre = genre, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Tek bir tür kartı: gradyan arka plan + sol üst köşede tür adı.
 *
 * Renk çifti modeldeki ARGB hex değerlerinden çizilir. Gerçek API görsel URL'si
 * sağladığında gradyan, görsel yükleyiciyle değiştirilebilir.
 */
@Composable
private fun GenreCard(
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(genre.artworkStartColor), Color(genre.artworkEndColor)),
                ),
            )
            .background(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                ),
            ),
    ) {
        Text(
            text = genre.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(14.dp),
        )
    }
}

// ── Preview ──

private val previewState = SearchUiState(
    searchQuery = "",
    selectedFilterId = "filter-all",
    filters = listOf(
        SearchFilter("filter-all", "Hepsi"),
        SearchFilter("filter-pop", "Pop"),
        SearchFilter("filter-electronic", "Elektronik"),
        SearchFilter("filter-acoustic", "Akustik"),
    ),
    genres = listOf(
        Genre("genre-pop", "Pop", 0xFF3DC5B0, 0xFF1A8A7A),
        Genre("genre-electronic", "Elektronik", 0xFF9B8EC4, 0xFF6B5FB8),
        Genre("genre-acoustic", "Akustik", 0xFFC488C0, 0xFF8B5A87),
        Genre("genre-lofi", "Lo-fi", 0xFF3A7B7B, 0xFF1E4D4D),
        Genre("genre-indie", "Indie", 0xFF6B5F99, 0xFF3E3670),
        Genre("genre-jazz", "Jazz", 0xFF4CAF50, 0xFF2E7D32),
        Genre("genre-classical", "Klasik", 0xFFC97BA0, 0xFF8B4570),
        Genre("genre-travel", "Yolculuk", 0xFFE8907A, 0xFFC06050),
    ),
)

@Preview(name = "Search - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun SearchScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        SearchScreen(state = previewState, onIntent = {})
    }
}

@Preview(name = "Search - Light", showBackground = true, showSystemUi = true)
@Composable
private fun SearchScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        SearchScreen(state = previewState, onIntent = {})
    }
}
