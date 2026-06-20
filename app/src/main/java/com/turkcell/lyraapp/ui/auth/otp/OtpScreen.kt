package com.turkcell.lyraapp.ui.auth.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun OtpRoute(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OtpEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is OtpEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                OtpEffect.NavigateToHome -> onNavigateToHome()
                OtpEffect.NavigateToCompleteProfile -> onNavigateToCompleteProfile()
            }
        }
    }

    OtpScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun OtpScreen(
    state: OtpUiState,
    onIntent: (OtpIntent) -> Unit,
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
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Doğrulama kodu",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = "${state.phoneNumber} numarasına gönderdiğimiz 6 haneli kodu gir.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(32.dp))

            OtpInputBox(
                code = state.code,
                onCodeChange = { onIntent(OtpIntent.CodeChanged(it)) }
            )
            
            Spacer(Modifier.height(32.dp))

            ResendPrompt(
                onResendClick = { onIntent(OtpIntent.ResendCode) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(Modifier.weight(1f))

            SubmitButton(
                enabled = state.isSubmitEnabled,
                isLoading = state.isLoading,
                onClick = { onIntent(OtpIntent.Submit) },
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OtpInputBox(
    code: String,
    onCodeChange: (String) -> Unit
) {
    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(6) { index ->
                    val char = when {
                        index < code.length -> code[index].toString()
                        else -> ""
                    }
                    val isFocused = index == code.length
                    
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFocused) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ResendPrompt(
    onResendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Kodu almadın mı?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Tekrar gönder",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onResendClick),
        )
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
                text = "Doğrula",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = LyraIcons.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(name = "OTP - Light", showBackground = true, showSystemUi = true)
@Composable
private fun OtpScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        OtpScreen(
            state = OtpUiState(phoneNumber = "+90 555 111 22 33", code = "123", isSubmitEnabled = false), 
            onIntent = {}, 
            onNavigateBack = {}
        )
    }
}

@Preview(name = "OTP - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun OtpScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        OtpScreen(
            state = OtpUiState(phoneNumber = "+90 555 111 22 33", code = "123456", isSubmitEnabled = true),
            onIntent = {},
            onNavigateBack = {}
        )
    }
}
