package com.turkcell.lyraapp.ui.notifications

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.notifications.NotificationPreference
import com.turkcell.lyraapp.data.notifications.NotificationPreferenceType
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun NotificationsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                NotificationsEffect.NavigateBack -> onNavigateBack()
                is NotificationsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is NotificationsEffect.ShowError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Tekrar dene",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(NotificationsIntent.Retry)
                    }
                }
            }
        }
    }

    NotificationsScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onIntent: (NotificationsIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bildirimler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(NotificationsIntent.BackClicked) }) {
                        Icon(
                            imageVector = LyraIcons.ArrowBack,
                            contentDescription = "Geri",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        if (state.isLoading && state.preferences.isEmpty()) {
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            ) {
                item { NotificationsHeader() }
                items(state.preferences, key = { it.id }) { preference ->
                    NotificationPreferenceRow(
                        preference = preference,
                        onToggle = {
                            onIntent(
                                NotificationsIntent.PreferenceToggled(
                                    preferenceId = preference.id,
                                    isEnabled = it,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 10.dp),
    ) {
        Text(
            text = "Bildirim tercihlerin",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "LyraApp'in sana ne zaman haber vereceğini buradan yönetebilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NotificationPreferenceRow(
    preference: NotificationPreference,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
    ) {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            leadingContent = {
                PreferenceIcon(icon = preference.type.icon())
            },
            headlineContent = {
                Text(
                    text = preference.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Text(
                    text = preference.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingContent = {
                Switch(
                    checked = preference.isEnabled,
                    onCheckedChange = onToggle,
                )
            },
        )
    }
}

@Composable
private fun PreferenceIcon(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

private fun NotificationPreferenceType.icon(): ImageVector =
    when (this) {
        NotificationPreferenceType.General -> LyraIcons.Notifications
        NotificationPreferenceType.NewMusic -> LyraIcons.Waveform
        NotificationPreferenceType.Recommendations -> LyraIcons.FavoriteOutlined
        NotificationPreferenceType.PlaylistUpdates -> LyraIcons.QueueMusic
        NotificationPreferenceType.Downloads -> LyraIcons.Download
        NotificationPreferenceType.ListeningReminders -> LyraIcons.Notifications
    }

private val previewState = NotificationsUiState(
    preferences = listOf(
        NotificationPreference(
            id = "general",
            title = "Genel bildirimler",
            description = "LyraApp bildirimlerini açıp kapatır.",
            type = NotificationPreferenceType.General,
            isEnabled = true,
        ),
        NotificationPreference(
            id = "new-music",
            title = "Yeni çıkan şarkılar",
            description = "Sevdiğin sanatçıların yeni şarkıları için haber verilir.",
            type = NotificationPreferenceType.NewMusic,
            isEnabled = true,
        ),
        NotificationPreference(
            id = "downloads",
            title = "İndirme bildirimleri",
            description = "İndirme tamamlandığında bildirim gösterilir.",
            type = NotificationPreferenceType.Downloads,
            isEnabled = false,
        ),
    ),
)

@Preview(name = "Notifications - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun NotificationsScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        NotificationsScreen(state = previewState, onIntent = {})
    }
}

@Preview(name = "Notifications - Light", showBackground = true, showSystemUi = true)
@Composable
private fun NotificationsScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        NotificationsScreen(state = previewState, onIntent = {})
    }
}
