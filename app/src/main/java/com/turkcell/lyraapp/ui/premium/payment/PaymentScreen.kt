package com.turkcell.lyraapp.ui.premium.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun PaymentRoute(
    planId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(planId) {
        viewModel.onIntent(PaymentIntent.LoadPlan(planId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PaymentEffect.NavigateBack -> onNavigateBack()
                is PaymentEffect.NavigateBackToHome -> {
                    if (effect.success) {
                        onNavigateToHome()
                    }
                }
                is PaymentEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    PaymentScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    state: PaymentUiState,
    onIntent: (PaymentIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ödeme", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(PaymentIntent.OnBackClick) }) {
                        Icon(imageVector = LyraIcons.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoadingPlan) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                MockCreditCard(
                    cardNumber = state.cardNumber,
                    cardName = state.cardName,
                    cardExpiry = state.cardExpiry
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                PaymentForm(state = state, onIntent = onIntent)

                Spacer(modifier = Modifier.height(32.dp))

                state.plan?.let { plan ->
                    PlanSummary(
                        planName = plan.name,
                        planPrice = plan.price
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onIntent(PaymentIntent.OnPayClick) },
                    enabled = state.isPayButtonEnabled && !state.isProcessingPayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (state.isProcessingPayment) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(imageVector = LyraIcons.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${state.plan?.price ?: ""} öde",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = LyraIcons.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ödemen 256-bit SSL ile güvende",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MockCreditCard(cardNumber: String, cardName: String, cardExpiry: String) {
    val formattedNumber = formatCardNumber(cardNumber)
    val formattedExpiry = formatExpiry(cardExpiry)
    val displayName = cardName.ifBlank { "AD SOYAD" }.uppercase()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF9E4856), Color(0xFFC07068))
                )
            )
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Chip
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFD700).copy(alpha = 0.8f))
                )
                // Logo placeholder
                Icon(
                    imageVector = LyraIcons.Star,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = formattedNumber,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "KART SAHİBİ",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = displayName,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SKT",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formattedExpiry,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentForm(state: PaymentUiState, onIntent: (PaymentIntent) -> Unit) {
    Column {
        Text("Kart numarası", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = formatCardNumber(state.cardNumber),
            onValueChange = { onIntent(PaymentIntent.CardNumberChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("0000 0000 0000 0000") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Kart üzerindeki isim", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.cardName,
            onValueChange = { onIntent(PaymentIntent.CardNameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Ad Soyad") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Son kullanma", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formatExpiry(state.cardExpiry),
                    onValueChange = { onIntent(PaymentIntent.CardExpiryChanged(it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("AA/YY") }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("CVC", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.cardCvc,
                    onValueChange = { onIntent(PaymentIntent.CardCvcChanged(it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("123") }
                )
            }
        }
    }
}

@Composable
private fun PlanSummary(planName: String, planPrice: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFDAB9)),
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
                        text = "LyraApp Premium",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = planName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = planPrice,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Bugün ödenecek",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = planPrice, // The string usually contains " / ay", in a real app we might parse it out, but this is fine
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

private fun formatCardNumber(number: String): String {
    if (number.isEmpty()) return "•••• •••• •••• ••••"
    val sb = java.lang.StringBuilder()
    for (i in number.indices) {
        sb.append(number[i])
        if ((i + 1) % 4 == 0 && i != number.lastIndex) {
            sb.append(" ")
        }
    }
    return sb.toString()
}

private fun formatExpiry(expiry: String): String {
    if (expiry.isEmpty()) return "AA/YY"
    if (expiry.length <= 2) return expiry
    return expiry.substring(0, 2) + "/" + expiry.substring(2)
}
