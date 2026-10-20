package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradingSignal
import com.example.data.repository.TradingSignalEngine
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.max

@Composable
fun VisualTradeProgressBar(
    signal: TradingSignal,
    modifier: Modifier = Modifier
) {
    val isStandby = signal.isStandby
    val isBullish = signal.isBullish
    val currentPrice = signal.currentPrice
    val entryPrice = signal.entryPrice
    val slPrice = signal.slPrice
    val tpPrice = signal.tpPrice

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBackground.copy(alpha = 0.7f))
            .padding(12.dp)
    ) {
        // Top Header Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isStandby) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "Standby",
                        tint = AmberWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "STANDBY ANALYSIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberWarning,
                        letterSpacing = 1.sp
                    )
                } else {
                    Icon(
                        imageVector = if (isBullish) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = if (isBullish) "Bullish" else "Bearish",
                        tint = if (isBullish) BullishGreen else BearishRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBullish) "LONG PROGRESS" else "SHORT PROGRESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBullish) BullishGreen else BearishRed,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (isStandby) {
                Text(
                    text = "Awaiting 09:00 Harare",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            } else {
                val pnl = signal.pnlPercent
                val pnlColor = if (pnl >= 0) BullishGreen else BearishRed
                val prefix = if (pnl >= 0) "+" else ""
                Text(
                    text = "$prefix${String.format(java.util.Locale.US, "%.2f", pnl)}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = pnlColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress Bar Visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkCardStroke)
        ) {
            if (isStandby) {
                // Standby pulsing / equilibrium indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(DarkCardStroke, AmberWarning.copy(alpha = 0.6f), AmberWarning)
                            )
                        )
                )
            } else {
                val totalDistance = max(0.00001, abs(tpPrice - entryPrice))
                val currentDistance = if (isBullish) {
                    (currentPrice - entryPrice).coerceIn(-totalDistance, totalDistance)
                } else {
                    (entryPrice - currentPrice).coerceIn(-totalDistance, totalDistance)
                }
                val fraction = ((currentDistance / totalDistance) * 0.5f + 0.5f).coerceIn(0.05, 0.95).toFloat()

                val barColor = if (currentDistance >= 0) BullishGreen else BearishRed
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(barColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Labels underneath: SL, Entry, TP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = "SL", fontSize = 10.sp, color = BearishRed, fontWeight = FontWeight.Bold)
                Text(
                    text = TradingSignalEngine.formatPrice(slPrice, signal.symbol),
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "ENTRY", fontSize = 10.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                Text(
                    text = TradingSignalEngine.formatPrice(entryPrice, signal.symbol),
                    fontSize = 11.sp,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "TP", fontSize = 10.sp, color = BullishGreen, fontWeight = FontWeight.Bold)
                Text(
                    text = TradingSignalEngine.formatPrice(tpPrice, signal.symbol),
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
