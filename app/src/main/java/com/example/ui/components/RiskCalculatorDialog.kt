package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.RiskCalcResult
import com.example.data.model.TradingSignal
import com.example.data.repository.TradingSignalEngine
import com.example.ui.theme.*
import kotlin.math.abs

@Composable
fun RiskCalculatorDialog(
    signal: TradingSignal? = null,
    balance: String = "10000",
    riskPercent: String = "1.5",
    calcResult: RiskCalcResult? = null,
    onBalanceChange: (String) -> Unit = {},
    onRiskPercentChange: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    var accountBalance by remember(balance) { mutableStateOf(balance) }
    var riskPct by remember(riskPercent) { mutableStateOf(riskPercent) }

    val sym = signal?.symbol ?: "EURUSD"
    val entryPrice = signal?.entryPrice ?: 1.0850
    val slPrice = signal?.slPrice ?: 1.0800
    val tpPrice = signal?.tpPrice ?: 1.0925

    val balanceVal = accountBalance.toDoubleOrNull() ?: 10000.0
    val riskVal = riskPct.toDoubleOrNull() ?: 1.5

    val riskDollar = balanceVal * (riskVal / 100.0)
    val pip = if (sym.contains("JPY")) 0.01 else 0.0001
    val slDistance = abs(entryPrice - slPrice)
    val slPips = if (slDistance > 0) slDistance / pip else 50.0

    val pipValuePerStandardLot = 10.0 // approx standard lot pip value in USD
    val calculatedLotSize = if (slPips > 0) {
        val lots = riskDollar / (slPips * pipValuePerStandardLot)
        String.format(java.util.Locale.US, "%.2f", lots.coerceIn(0.01, 50.0))
    } else {
        "0.10"
    }

    val tpDistance = abs(tpPrice - entryPrice)
    val tpPips = if (tpDistance > 0) tpDistance / pip else 75.0
    val rewardDollar = (calculatedLotSize.toDoubleOrNull() ?: 0.1) * tpPips * pipValuePerStandardLot

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardStroke),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCT Risk Calculator",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Symbol Info Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Target Pair", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = sym,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Inputs
                OutlinedTextField(
                    value = accountBalance,
                    onValueChange = {
                        accountBalance = it
                        onBalanceChange(it)
                    },
                    label = { Text("Account Balance (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCardStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = riskPct,
                    onValueChange = {
                        riskPct = it
                        onRiskPercentChange(it)
                    },
                    label = { Text("Risk Per Trade (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCardStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Calculation Result Cards
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardStroke),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Max Dollar Risk", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "$${String.format(java.util.Locale.US, "%.2f", riskDollar)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BearishRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Stop Loss Distance", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "${String.format(java.util.Locale.US, "%.1f", slPips)} pips",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Est. Reward", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "+$${String.format(java.util.Locale.US, "%.2f", rewardDollar)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BullishGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = DarkCardStroke
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("RECOMMENDED LOT", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text("Standard Lots", fontSize = 10.sp, color = TextMuted)
                            }
                            Text(
                                text = "$calculatedLotSize lots",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply & Close", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
