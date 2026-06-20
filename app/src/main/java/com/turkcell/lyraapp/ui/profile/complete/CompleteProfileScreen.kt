package com.turkcell.lyraapp.ui.profile.complete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun CompleteProfileRoute(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompleteProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CompleteProfileEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                CompleteProfileEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    CompleteProfileScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun CompleteProfileScreen(
    state: CompleteProfileUiState,
    onIntent: (CompleteProfileIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = LyraIcons.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                // 3/3 Indicator
                Text(
                    text = "3 / 3",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Bilgilerini tamamla",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = "Hoş geldin! Profilini oluşturmak için birkaç bilgiye ihtiyacımız var.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { onIntent(CompleteProfileIntent.FirstNameChanged(it)) },
                    label = { Text("Ad") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = { onIntent(CompleteProfileIntent.LastNameChanged(it)) },
                    label = { Text("Soyad") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "Doğum tarihi",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.day,
                    onValueChange = { onIntent(CompleteProfileIntent.DayChanged(it)) },
                    placeholder = { Text("GG") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = state.month,
                    onValueChange = { onIntent(CompleteProfileIntent.MonthChanged(it)) },
                    placeholder = { Text("AA") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = state.year,
                    onValueChange = { onIntent(CompleteProfileIntent.YearChanged(it)) },
                    placeholder = { Text("YYYY") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.5f),
                    singleLine = true
                )
            }
            
            Spacer(Modifier.weight(1f))

            SubmitButton(
                enabled = state.isSubmitEnabled,
                isLoading = state.isLoading,
                onClick = { onIntent(CompleteProfileIntent.Submit) },
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SubmitButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
        } else {
            Text(
                text = "Tamamla",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = LyraIcons.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(name = "Complete Profile - Light", showBackground = true, showSystemUi = true)
@Composable
private fun CompleteProfileScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        CompleteProfileScreen(
            state = CompleteProfileUiState(firstName = "Zeynep", lastName = "Kaya", day = "14", month = "06", year = "1998", isSubmitEnabled = true), 
            onIntent = {}, 
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Complete Profile - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun CompleteProfileScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        CompleteProfileScreen(
            state = CompleteProfileUiState(),
            onIntent = {},
            onNavigateBack = {}
        )
    }
}
