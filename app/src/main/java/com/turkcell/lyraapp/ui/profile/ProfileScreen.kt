package com.turkcell.lyraapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.profile.UserProfile
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun ProfileRoute(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPremiumPlans: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.NavigateToNotifications -> onNavigateToNotifications()
                is ProfileEffect.NavigateToPremiumPlans -> {
                    onNavigateToPremiumPlans()
                }
                is ProfileEffect.NavigateToLogin -> {
                    onNavigateToLogin()
                }
                is ProfileEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    ProfileScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { onIntent(ProfileIntent.OnEditProfileClick) }) {
                        Icon(
                            imageVector = LyraIcons.Edit,
                            contentDescription = "Profili Düzenle",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { onIntent(ProfileIntent.OnSettingsClick) }) {
                        Icon(
                            imageVector = LyraIcons.Settings,
                            contentDescription = "Ayarlar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.profileInfo != null) {
            val profile = state.profileInfo
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ProfileHeader(profile = profile)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ProfileStats(profile = profile)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    PremiumBanner(
                        profile = profile,
                        onClick = { onIntent(ProfileIntent.OnPremiumBannerClick) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    ThemeToggleSection(
                        isDarkMode = state.isDarkMode,
                        onThemeToggle = { onIntent(ProfileIntent.OnThemeToggle(it)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ProfileMenuItems(onIntent = onIntent)
                }
            }

            if (state.showEditDialog) {
                EditProfileDialog(
                    profile = profile,
                    isSaving = state.isSaving,
                    onDismiss = { onIntent(ProfileIntent.OnDismissEditDialog) },
                    onSave = { firstName, lastName, birthDate ->
                        onIntent(ProfileIntent.OnSaveProfile(firstName, lastName, birthDate))
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(profile: UserProfile) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF8B6FB8), Color(0xFF8A5526))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = profile.initials,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = profile.name,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "@${profile.username} · ${if (profile.isPremium) "Premium" else "Ücretsiz"}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = profile.phone,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ProfileStats(profile: UserProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(value = profile.playlistCount.toString(), label = "Çalma listesi")
        StatItem(value = profile.followersCount, label = "Takipçi")
        StatItem(value = profile.followingCount.toString(), label = "Takip")
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ThemeToggleSection(isDarkMode: Boolean, onThemeToggle: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Görünüm",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (!isDarkMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onThemeToggle(false) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = LyraIcons.LightMode,
                        contentDescription = "Açık",
                        tint = if (!isDarkMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Açık",
                        color = if (!isDarkMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDarkMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onThemeToggle(true) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = LyraIcons.DarkMode,
                        contentDescription = "Koyu",
                        tint = if (isDarkMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Koyu",
                        color = if (isDarkMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItems(onIntent: (ProfileIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ProfileMenuItem(
            icon = LyraIcons.Waveform,
            title = "Ses kalitesi",
            value = "Yüksek",
            onClick = { onIntent(ProfileIntent.OnAudioQualityClick) }
        )
        ProfileMenuItem(
            icon = LyraIcons.Download,
            title = "Çevrimdışı indirme",
            value = "Açık",
            onClick = { onIntent(ProfileIntent.OnOfflineDownloadClick) }
        )
        ProfileMenuItem(
            icon = LyraIcons.Notifications,
            title = "Bildirimler",
            onClick = { onIntent(ProfileIntent.OnNotificationsClick) }
        )
        ProfileMenuItem(
            icon = LyraIcons.Lock,
            title = "Gizlilik",
            onClick = { onIntent(ProfileIntent.OnPrivacyClick) }
        )
        ProfileMenuItem(
            icon = LyraIcons.HelpOutline,
            title = "Yardım ve destek",
            onClick = { onIntent(ProfileIntent.OnHelpClick) }
        )
        ProfileMenuItem(
            icon = LyraIcons.Block,
            title = "Çıkış Yap",
            onClick = { onIntent(ProfileIntent.OnLogoutClick) },
            textColor = MaterialTheme.colorScheme.error,
            iconColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Icon(
            imageVector = LyraIcons.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EditProfileDialog(
    profile: UserProfile,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var firstName by remember { mutableStateOf(profile.firstName ?: "") }
    var lastName by remember { mutableStateOf(profile.lastName ?: "") }
    var birthDate by remember { mutableStateOf(profile.birthDate ?: "") }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(text = "Profili Düzenle") },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Ad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Soyad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Doğum Tarihi (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(firstName, lastName, birthDate) },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Kaydet")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun PremiumBanner(profile: UserProfile, onClick: () -> Unit) {
    val membership = profile.membership
    
    val daysLeft = remember(membership) {
        if (membership?.expiresAt != null) {
            try {
                val expiresInstant = java.time.Instant.parse(membership.expiresAt)
                val now = java.time.Instant.now()
                val diff = java.time.Duration.between(now, expiresInstant).toDays()
                diff.coerceAtLeast(0)
            } catch (e: Exception) {
                0L
            }
        } else {
            0L
        }
    }

    val isPremium = profile.isPremium
    val showBanner = true

    if (showBanner) {
        val title = when {
            !isPremium -> "LyraApp Premium'u Keşfet"
            daysLeft <= 3 -> "Premium · $daysLeft gün kaldı"
            else -> "Premium Üye"
        }
        val subtitle = when {
            !isPremium -> "Reklamsız ve sınırsız müziğin keyfini çıkar"
            daysLeft <= 3 -> "Aboneliğiniz bitmek üzere, yenileyin"
            else -> "Aboneliğinizin bitmesine $daysLeft gün kaldı"
        }
        
        val isClickable = !isPremium || daysLeft <= 3

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFFB6C1), Color(0xFFFFDAB9))
                    )
                )
                .clickable(enabled = isClickable, onClick = onClick)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LyraIcons.Star,
                        contentDescription = "Premium",
                        tint = Color(0xFF6A1B9A),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6A1B9A).copy(alpha = 0.8f)
                    )
                }
                
                if (isClickable) {
                    Icon(
                        imageVector = LyraIcons.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF6A1B9A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

