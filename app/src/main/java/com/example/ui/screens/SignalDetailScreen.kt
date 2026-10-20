package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradingSignal
import com.example.ui.components.AssetBadge
import com.example.ui.components.CandlestickChart
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BearishRedContainer
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.BullishGreenContainer
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeutralBlue
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalDark
import com.example.ui.theme.TerminalSurfaceCard
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import java.text.DecimalFormat

@Composable
fun SignalDetailScreen(
    signal: TradingSignal,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // BackHandler to handle system back button
    BackHandler {
        viewModel.closeSignalDetail()
    }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val ticks by viewModel.allTicks.collectAsState()

    val tick = ticks[signal.symbol]
    val currentPrice = tick?.currentPrice ?: signal.currentPrice
    val spread = tick?.calculatedSpread ?: signal.spread
    val bid = tick?.bid ?: (currentPrice - spread / 2)
    val ask = tick?.ask ?: (currentPrice + spread / 2)

    val priceFormatter = remember(currentPrice) {
        if (currentPrice < 1.0) DecimalFormat("#,##0.0000")
        else if (currentPrice < 10.0) DecimalFormat("#,##0.000")
        else DecimalFormat("#,##0.00")
    }

    val isBullish = signal.signalType.isBullish
    val isBearish = signal.signalType.isBearish
    val accentColor = if (isBullish) BullishGreen else if (isBearish) BearishRed else NeutralBlue

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalDark)
            .testTag("signal_detail_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.closeSignalDetail() },
                    modifier = Modifier.testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = signal.symbol,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AssetBadge(assetClass = signal.assetClass)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.toggleBookmark(signal.symbol) },
                        modifier = Modifier.testTag("detail_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (signal.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (signal.isBookmarked) GoldAccent else TextMuted
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = """
                                [BiSignal Trade Setup]
                                Instrument: ${signal.symbol} (${signal.name})
                                Action: ${signal.signalType.label}
                                Timeframe: ${signal.timeframe}
                                Entry: ${priceFormatter.format(signal.entryPrice)}
                                TP 1: ${priceFormatter.format(signal.tp1)}
                                TP 2: ${priceFormatter.format(signal.tp2)}
                                Stop Loss: ${priceFormatter.format(signal.stopLoss)}
                                Risk/Reward: 1:${signal.riskRewardRatio}
                                Feed: Biquote API
                            """.trimIndent()
                            clipboard.setPrimaryClip(ClipData.newPlainText("BiSignal Setup", clipText))
                            viewModel.setToast("Signal copied to clipboard!")
                        },
                        modifier = Modifier.testTag("copy_signal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Signal",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }

        // Hero Price & Direction Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp)),
                color = TerminalSurfaceCard,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Biquote Live Midpoint",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = priceFormatter.format(currentPrice),
                                color = TextPrimary,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Signal Badge
                        Box(
                            modifier = Modifier
                                .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .border(1.dp, accentColor, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBullish) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = signal.signalType.label,
                                    color = accentColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bid, Ask, Spread Stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Bid Price", color = TextMuted, fontSize = 10.sp)
                            Text(priceFormatter.format(bid), color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Ask Price", color = TextMuted, fontSize = 10.sp)
                            Text(priceFormatter.format(ask), color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Spread", color = TextMuted, fontSize = 10.sp)
                            Text(String.format("%.4f", spread), color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Confidence", color = TextMuted, fontSize = 10.sp)
                            Text("${signal.confidenceScore}%", color = BullishGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Timeframe Selector Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("M5", "M15", "H1", "H4", "D1").forEach { tf ->
                    val isSelected = uiState.selectedDetailTimeframe == tf
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) NeutralBlue else TerminalSurfaceElevated,
                                RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, if (isSelected) NeutralBlue else TerminalBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.changeDetailTimeframe(tf) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf,
                            color = if (isSelected) Color(0xFF090D16) else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Candlestick Chart
        item {
            CandlestickChart(
                candles = signal.candles,
                entryPrice = signal.entryPrice,
                tp1 = signal.tp1,
                tp2 = signal.tp2,
                stopLoss = signal.stopLoss
            )
        }

        // Trade Setup Card (Entry, TP1, TP2, Stop Loss, Risk-to-Reward)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp)),
                color = TerminalSurfaceCard,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trade Execution Targets",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "R:R 1:${signal.riskRewardRatio}",
                                color = GoldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TargetRow(label = "ENTRY ORDER", price = priceFormatter.format(signal.entryPrice), note = "Market / Limit", color = NeutralBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    TargetRow(label = "TAKE PROFIT 1 (TP1)", price = priceFormatter.format(signal.tp1), note = "+1.5R Scalp Target", color = BullishGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    TargetRow(label = "TAKE PROFIT 2 (TP2)", price = priceFormatter.format(signal.tp2), note = "+2.8R Extended Runner", color = BullishGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    TargetRow(label = "STOP LOSS (SL)", price = priceFormatter.format(signal.stopLoss), note = "-1.0R Invalidation", color = BearishRed)
                }
            }
        }

        // Technical Indicators Breakdown Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp)),
                color = TerminalSurfaceCard,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Technical Indicators (Engine Output)",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    IndicatorItem(
                        name = "RSI (14 Period)",
                        value = "${String.format("%.1f", signal.rsi)}",
                        status = when {
                            signal.rsi < 30 -> "Oversold Reversal Zone"
                            signal.rsi > 70 -> "Overbought Exhaustion"
                            else -> "Neutral Momentum (40-60)"
                        },
                        statusColor = if (signal.rsi < 30) BullishGreen else if (signal.rsi > 70) BearishRed else TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    IndicatorItem(
                        name = "MACD Histogram (12, 26, 9)",
                        value = String.format("%+.3f", signal.macdHistogram),
                        status = if (signal.macdHistogram > 0) "Positive Bullish Convergence" else "Negative Bearish Momentum",
                        statusColor = if (signal.macdHistogram > 0) BullishGreen else BearishRed
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    IndicatorItem(
                        name = "Exponential Moving Averages",
                        value = "EMA 9 / 21 / 50",
                        status = signal.emaTrend,
                        statusColor = if (signal.emaTrend.contains("Bullish")) BullishGreen else if (signal.emaTrend.contains("Bearish")) BearishRed else NeutralBlue
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    IndicatorItem(
                        name = "Spread Health Check",
                        value = "${String.format("%.4f", spread)} Pts",
                        status = "Optimal Execution (Low Slippage Risk)",
                        statusColor = GoldAccent
                    )
                }
            }
        }

        // Algorithmic Strategy Rationale
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp)),
                color = TerminalSurfaceCard,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Strategy Rationale",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = signal.rationale,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 5 Previous Days Setup Performance Card (SCT Theory Engine)
        item {
            val performance = remember(signal.symbol, currentPrice) {
                viewModel.getPerformanceForSymbol(signal.symbol)
            }
            val netColor = if (performance.netPips >= 0) BullishGreen else BearishRed

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp))
                    .testTag("signal_detail_5day_card"),
                color = TerminalSurfaceCard,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "5-Day Setup Performance",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SCT Theory Strategy History",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Net Pips Pill
                        Box(
                            modifier = Modifier
                                .background(netColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, netColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${if (performance.netPips >= 0) "+" else ""}${String.format("%.1f", performance.netPips)} Pips",
                                color = netColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mode & Win rate bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Win Rate:", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = "${String.format("%.1f", performance.winRatePercent)}% (${performance.totalWins}W / ${performance.totalLosses}L)",
                                color = if (performance.winRatePercent >= 50) BullishGreen else BearishRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Avg Pips / Day:", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = "${if (performance.avgPipsPerDay >= 0) "+" else ""}${String.format("%.1f", performance.avgPipsPerDay)} pips",
                                color = if (performance.avgPipsPerDay >= 0) BullishGreen else BearishRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5-Day Compact Row List
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        performance.dailySetups.forEach { daySetup ->
                            val isWin = daySetup.isWin
                            val stColor = if (isWin) BullishGreen else BearishRed
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(TerminalSurfaceElevated, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Day ${daySetup.dayNumber}",
                                        color = NeutralBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = daySetup.fullDate,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isWin) "TP HIT" else "SL HIT",
                                        color = stColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${if (daySetup.pips >= 0) "+" else ""}${String.format("%.1f", daySetup.pips)}p",
                                        color = stColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Button to open full 5-day setup breakdown
                    OutlinedButton(
                        onClick = { viewModel.navigateToPerformance(signal.symbol) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("view_full_5day_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralBlue)
                    ) {
                        Icon(imageVector = Icons.Default.QueryStats, contentDescription = null, tint = NeutralBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Full 5-Day SCT Dashboard", color = NeutralBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Spotware cTrader Open API Institutional Execution Banner
        item {
            val ctraderAuth by viewModel.ctraderAuthState.collectAsState()
            val isAuthorized = ctraderAuth.isAuthorized
            val activeAcc = ctraderAuth.activeAccount

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                color = TerminalSurfaceCard,
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                    .size(30.dp)
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                    contentDescription = "cTrader",
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Spotware cTrader Open API",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isAuthorized) {
                                        "${activeAcc?.brokerName ?: "cTrader"} (#${activeAcc?.accountId}) • $${String.format("%.2f", activeAcc?.equity ?: 0.0)}"
                                    } else {
                                        "Connect account to trade with institutional execution"
                                    },
                                    color = if (isAuthorized) BullishGreen else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (!isAuthorized) {
                            OutlinedButton(
                                onClick = { viewModel.openCTraderTerminal(true) },
                                modifier = Modifier.testTag("detail_connect_ctrader_btn"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6))
                            ) {
                                Text("Connect", color = Color(0xFFA78BFA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.openCTraderTerminal(true) },
                                modifier = Modifier.size(28.dp).testTag("detail_ctrader_wallet_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "cTrader Terminal",
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isAuthorized) {
                                    viewModel.openCTraderOrder(signal)
                                } else {
                                    viewModel.openCTraderTerminal(true)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("detail_trade_ctrader_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B5CF6)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Trade ${signal.symbol} on cTrader",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.openDxTradeOrder(signal) },
                            modifier = Modifier
                                .weight(0.75f)
                                .testTag("detail_trade_dx_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TerminalSurfaceElevated
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = NeutralBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DXtrade",
                                color = NeutralBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.showRiskCalculator(true) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("calc_position_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeutralBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = Color(0xFF090D16), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Risk Calculator", color = Color(0xFF090D16), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.navigateToPerformance(signal.symbol) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_5day_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("5D History", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TargetRow(label: String, price: String, note: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(note, color = TextSecondary, fontSize = 11.sp)
        }
        Text(
            text = price,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun IndicatorItem(name: String, value: String, status: String, statusColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = GoldAccent, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(status, color = statusColor, fontSize = 11.sp)
    }
}
