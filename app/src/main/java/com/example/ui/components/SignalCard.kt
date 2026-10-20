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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetClass
import com.example.data.model.SignalType
import com.example.data.model.TradingSignal
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BearishRedContainer
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.BullishGreenContainer
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeutralBlue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurfaceCard
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun SignalCard(
    signal: TradingSignal,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onTradeClick: (() -> Unit)? = null,
    onCTraderClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val priceFormatter = remember(signal.currentPrice) {
        if (signal.currentPrice < 1.0) DecimalFormat("#,##0.0000")
        else if (signal.currentPrice < 10.0) DecimalFormat("#,##0.000")
        else DecimalFormat("#,##0.00")
    }

    val isBullish = signal.signalType.isBullish
    val isBearish = signal.signalType.isBearish

    val accentColor = when {
        isBullish -> BullishGreen
        isBearish -> BearishRed
        else -> NeutralBlue
    }

    val containerBg = when {
        isBullish -> BullishGreenContainer.copy(alpha = 0.35f)
        isBearish -> BearishRedContainer.copy(alpha = 0.35f)
        else -> TerminalSurfaceElevated
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("signal_card_${signal.symbol}"),
        color = TerminalSurfaceCard,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Symbol, Asset Class badge, Timeframe & Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Asset icon bullet
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(containerBg, CircleShape)
                            .border(1.dp, accentColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBullish) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = signal.signalType.label,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = signal.symbol,
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            // Asset Class Badge
                            AssetBadge(assetClass = signal.assetClass)
                        }
                        Text(
                            text = signal.name,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Timeframe Tag
                    Box(
                        modifier = Modifier
                            .background(TerminalSurfaceElevated, RoundedCornerShape(6.dp))
                            .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = signal.timeframe,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onBookmarkToggle,
                        modifier = Modifier.testTag("bookmark_button_${signal.symbol}")
                    ) {
                        Icon(
                            imageVector = if (signal.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (signal.isBookmarked) "Remove bookmark" else "Bookmark",
                            tint = if (signal.isBookmarked) GoldAccent else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Signal Action Bar: Signal Pill, Confidence, and Live Price
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurfaceElevated, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Signal Direction Pill
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .border(1.dp, accentColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = signal.signalType.label,
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Confidence
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Confidence",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${signal.confidenceScore}%",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Live Biquote Price
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Biquote Mid",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = priceFormatter.format(signal.currentPrice),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Targets Grid: Entry, TP1, TP2, Stop Loss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TargetColumn(label = "ENTRY", price = priceFormatter.format(signal.entryPrice), color = NeutralBlue)
                TargetColumn(label = "TP 1 (LIQUIDITY)", price = priceFormatter.format(signal.tp1), color = BullishGreen)
                TargetColumn(label = "TP 2", price = priceFormatter.format(signal.tp2), color = BullishGreen)
                TargetColumn(label = "STOP LOSS", price = priceFormatter.format(signal.stopLoss), color = BearishRed)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SCT Theory Institutional Strip
            signal.sctLevels?.let { sct ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color(0xFFA78BFA), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "ASIAN BOX",
                                color = Color(0xFFA78BFA),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${priceFormatter.format(sct.asianLow)} - ${priceFormatter.format(sct.asianHigh)}",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = if (signal.signalType.isBullish) "PDH: ${priceFormatter.format(sct.pdh)}" else "PDL: ${priceFormatter.format(sct.pdl)}",
                        color = Color(0xFFF43F5E),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Footer row: R:R ratio, Spread, SCT Strategy Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "R:R 1:${signal.riskRewardRatio}",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Spread: ${String.format("%.4f", signal.spread)}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = signal.sctStrategyType.take(28),
                    color = NeutralBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // DXtrade Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (signal.sctLifecycle.isNotEmpty()) "Protocol: ${signal.sctLifecycle.replace('_', ' ')}" else "SCT Ready",
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onCTraderClick != null) {
                        Button(
                            onClick = onCTraderClick,
                            modifier = Modifier.testTag("trade_ctrader_btn_${signal.symbol}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B5CF6).copy(alpha = 0.20f)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = "Trade on cTrader",
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "cTrader",
                                color = Color(0xFFA78BFA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { onTradeClick?.invoke() ?: onClick() },
                        modifier = Modifier.testTag("trade_dxtrade_btn_${signal.symbol}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor.copy(alpha = 0.18f)
                        ),
                        border = BorderStroke(1.dp, accentColor),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Trade DXtrade",
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBullish) "TRADE BUY" else if (isBearish) "TRADE SELL" else "TRADE DX",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssetBadge(assetClass: AssetClass) {
    val color = when (assetClass) {
        AssetClass.CRYPTO -> Color(0xFF8B5CF6)
        AssetClass.FOREX -> Color(0xFF3B82F6)
        AssetClass.METALS -> GoldAccent
        AssetClass.INDICES -> Color(0xFF10B981)
        AssetClass.ALL -> TextSecondary
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .border(0.5.dp, color.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(
            text = assetClass.displayName.uppercase(),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TargetColumn(label: String, price: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = price,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
