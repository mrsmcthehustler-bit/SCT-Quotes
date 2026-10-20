package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CTraderAccount
import com.example.data.model.CTraderOrderRequest
import com.example.data.model.TradingSignal
import com.example.data.repository.TradingSignalEngine
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeutralBlue
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalDark
import com.example.ui.theme.TerminalSurfaceCard
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CTraderOrderDialog(
    signal: TradingSignal,
    account: CTraderAccount?,
    onExecuteOrder: (CTraderOrderRequest) -> Unit,
    onOpenConnectDialog: () -> Unit,
    onDismiss: () -> Unit
) {
    val isBullish = signal.signalType.isBullish
    var volumeLots by remember { mutableDoubleStateOf(0.10) }
    var stopLossText by remember { mutableStateOf(TradingSignalEngine.formatPrice(signal.stopLoss, signal.symbol)) }
    var takeProfitText by remember { mutableStateOf(TradingSignalEngine.formatPrice(signal.tp1, signal.symbol)) }

    val currentPrice = signal.currentPrice
    val point = when {
        signal.symbol.contains("JPY") -> 0.01
        currentPrice < 10.0 -> 0.0001
        currentPrice < 100.0 -> 0.01
        else -> 0.1
    }

    val slVal = stopLossText.toDoubleOrNull() ?: signal.stopLoss
    val tpVal = takeProfitText.toDoubleOrNull() ?: signal.tp1

    val slDistancePips = abs(currentPrice - slVal) / point
    val tpDistancePips = abs(tpVal - currentPrice) / point

    val pipValueUsd = volumeLots * 10.0
    val riskUsd = slDistancePips * pipValueUsd
    val rewardUsd = tpDistancePips * pipValueUsd

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("ctrader_order_dialog")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, TerminalBorder, RoundedCornerShape(20.dp)),
            color = TerminalSurfaceCard,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(NeutralBlue.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, NeutralBlue.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = null,
                                tint = NeutralBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "cTrader Trade Execution",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Spotware Open API Gateway",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_ctrader_order_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Connection Banner
                if (account != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                            .border(0.5.dp, if (account.isLive) BullishGreen.copy(alpha = 0.4f) else GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (account.isLive) "LIVE" else "DEMO",
                                    color = if (account.isLive) BullishGreen else GoldAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = account.accountNumber,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = account.brokerName,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Free Margin", color = TextMuted, fontSize = 9.sp)
                            Text(
                                text = "$${DecimalFormat("#,##0.00").format(account.freeMargin)}",
                                color = BullishGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF59E0B).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onOpenConnectDialog() }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No cTrader account connected. Tap to connect.",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Connect", color = NeutralBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Symbol, Direction & Current Price
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = signal.symbol,
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isBullish) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(1.dp, if (isBullish) BullishGreen else BearishRed, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isBullish) "BUY" else "SELL",
                                    color = if (isBullish) BullishGreen else BearishRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Text(
                            text = signal.sctStrategyType,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = TradingSignalEngine.formatPrice(currentPrice, signal.symbol),
                            color = if (isBullish) BullishGreen else BearishRed,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Spread: ${String.format("%.1f", signal.spread * 10000)} pts",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Volume (Lots) selector
                Text("Volume (Lots)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.01, 0.05, 0.10, 0.50, 1.00).forEach { lot ->
                        val isSelected = volumeLots == lot
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeutralBlue else TerminalSurfaceElevated)
                                .border(1.dp, if (isSelected) NeutralBlue else TerminalBorder, RoundedCornerShape(6.dp))
                                .clickable { volumeLots = lot }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%.2f", lot),
                                color = if (isSelected) TerminalDark else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stop Loss & Take Profit Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stop Loss
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = BearishRed, modifier = Modifier.size(12.dp))
                            Text("Stop Loss", color = BearishRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            valText = stopLossText,
                            onValueChange = { stopLossText = it },
                            placeholder = "Stop Loss",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "-${String.format("%.1f", slDistancePips)}p (~$${String.format("%.2f", riskUsd)})",
                            color = BearishRed,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Take Profit
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(12.dp))
                            Text("Take Profit", color = BullishGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            valText = takeProfitText,
                            onValueChange = { takeProfitText = it },
                            placeholder = "Take Profit",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "+${String.format("%.1f", tpDistancePips)}p (~$${String.format("%.2f", rewardUsd)})",
                            color = BullishGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Execute Button
                Button(
                    onClick = {
                        val req = CTraderOrderRequest(
                            symbol = signal.symbol,
                            isBuy = isBullish,
                            volumeLots = volumeLots,
                            entryPrice = currentPrice,
                            stopLoss = slVal,
                            takeProfit = tpVal,
                            orderType = "MARKET",
                            sctStrategyOrigin = signal.sctStrategyType,
                            comment = "SCT ${signal.symbol} ${if (isBullish) "BUY" else "SELL"}"
                        )
                        onExecuteOrder(req)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("execute_ctrader_order_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBullish) BullishGreen else BearishRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Place ${if (isBullish) "BUY" else "SELL"} Order on cTrader (${volumeLots} Lots)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun OutlinedTextField(
    valText: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valText,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 11.sp, color = TextMuted) },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 13.sp,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeutralBlue,
            unfocusedBorderColor = TerminalBorder,
            focusedContainerColor = TerminalSurfaceElevated,
            unfocusedContainerColor = TerminalSurfaceElevated
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    )
}
