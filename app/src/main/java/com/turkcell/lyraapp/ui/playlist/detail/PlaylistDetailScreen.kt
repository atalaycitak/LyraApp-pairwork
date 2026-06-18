package com.turkcell.lyraapp.ui.playlist.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.turkcell.lyraapp.ui.icons.LyraIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.turkcell.lyraapp.data.playlist.SongItem
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PlaylistDetailRoute(
    playlistId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    PlaylistDetailScreen(
        playlistId = playlistId,
        onNavigateBack = onNavigateBack,
        onNavigateToPlayer = onNavigateToPlayer,
        viewModel = viewModel
    )
}

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    viewModel: PlaylistDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(playlistId) {
        viewModel.onIntent(PlaylistDetailIntent.LoadPlaylist(playlistId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlaylistDetailEffect.NavigateBack -> onNavigateBack()
                is PlaylistDetailEffect.NavigateToPlayer -> onNavigateToPlayer(effect.songId)
                is PlaylistDetailEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    // Ekran arkaplan rengi: üstte koyu pembe/kahvemsi bir ton, alta dogru siyah (dark mode varsayimi)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF3E2723), Color(0xFF121212))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        } else if (uiState.playlistDetail != null) {
            val detail = uiState.playlistDetail!!
            
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.onIntent(PlaylistDetailIntent.OnBackClick) }) {
                        Icon(imageVector = LyraIcons.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.onIntent(PlaylistDetailIntent.OnMoreClick) }) {
                        Icon(imageVector = LyraIcons.MoreVert, contentDescription = "Daha Fazla", tint = Color.White)
                    }
                }

                // Header Content
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Kapak Görseli
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(detail.coverStartColor),
                                        Color(detail.coverEndColor)
                                    )
                                )
                            )
                    ) {
                        // Görseldeki dairesel efektler (Basitce simüle edildi)
                        Box(modifier = Modifier.size(150.dp).align(Alignment.Center).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)))
                        Box(modifier = Modifier.size(100.dp).align(Alignment.Center).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = detail.title,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = detail.description,
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${detail.creator} • ${detail.songCount} şarkı • ${detail.totalDuration}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = { /* Like Playlist */ }) {
                            Icon(imageVector = LyraIcons.FavoriteOutlined, contentDescription = "Beğen", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.onIntent(PlaylistDetailIntent.OnDownloadClick) }) {
                            Icon(imageVector = LyraIcons.KeyboardArrowDown, contentDescription = "İndir", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.onIntent(PlaylistDetailIntent.OnAddClick) }) {
                            Icon(imageVector = LyraIcons.Add, contentDescription = "Ekle", tint = Color.White)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = { viewModel.onIntent(PlaylistDetailIntent.OnShuffleClick) }) {
                            Icon(imageVector = LyraIcons.Shuffle, contentDescription = "Karıştır", tint = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB6C1)) // Pembe oynat butonu
                                .clickable { viewModel.onIntent(PlaylistDetailIntent.OnPlayClick) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LyraIcons.PlayArrow,
                                contentDescription = "Oynat",
                                tint = Color(0xFF880E4F),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Songs List (siyahımsı arka plan)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.5f))
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(detail.songs) { song ->
                            SongListItem(
                                song = song,
                                onClick = { viewModel.onIntent(PlaylistDetailIntent.OnSongClick(song.id)) },
                                onLikeClick = { viewModel.onIntent(PlaylistDetailIntent.OnLikeSongClick(song.id)) }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SongListItem(
    song: SongItem,
    onClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Song Cover
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(song.coverStartColor),
                            Color(song.coverEndColor)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (song.isPlaying) {
                // Oynatılan şarkı için ikon (equilizer benzeri)
                Icon(
                    imageVector = LyraIcons.Waveform,
                    contentDescription = "Oynatılıyor",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Song Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = if (song.isPlaying) Color(0xFFFFB6C1) else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Duration
        Text(
            text = song.duration,
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Like Button
        IconButton(onClick = onLikeClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (song.isLiked) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                contentDescription = "Beğen",
                tint = if (song.isLiked) Color(0xFFFFB6C1) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        // More Button
        IconButton(onClick = { /* TODO: Song Options */ }, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = LyraIcons.MoreVert,
                contentDescription = "Daha Fazla",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
