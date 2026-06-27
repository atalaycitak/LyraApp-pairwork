package com.turkcell.lyraapp.ui.components.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun GlobalPremiumRenewalManager(
    onNavigateToPayment: (String) -> Unit,
    viewModel: PremiumRenewalViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkRenewalIfNeeded()
    }

    if (state.showDialog) {
        Dialog(
            onDismissRequest = { viewModel.dismissDialog() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B6FB8).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Bir ikon yoksa yildiz ikonu koyalim, ozel tasarimda saat ikonu vardi
                        Icon(
                            imageVector = LyraIcons.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB6C1),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Premium'un ${state.daysLeft} gün sonra bitiyor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tek seferlik erişimin sona ermek üzere. Kesintisiz dinlemeye devam etmek için yenile ya da aylık aboneliğe geç.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Aylık aboneliğe geç
                    state.monthlyPlan?.let { plan ->
                        Button(
                            onClick = { 
                                viewModel.dismissDialog()
                                onNavigateToPayment(plan.id) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB6C1),
                                contentColor = Color(0xFF6A1B9A)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                text = "Aylık aboneliğe geç · ₺${plan.price}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 30 gün yenile
                    state.oneTimePlan?.let { plan ->
                        OutlinedButton(
                            onClick = { 
                                viewModel.dismissDialog()
                                onNavigateToPayment(plan.id) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = "30 gün yenile · ₺${plan.price}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Daha sonra
                    TextButton(onClick = { viewModel.dismissDialog() }) {
                        Text(
                            text = "Daha sonra",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
