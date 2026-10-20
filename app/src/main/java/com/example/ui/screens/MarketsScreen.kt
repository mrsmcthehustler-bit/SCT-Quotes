package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetClass
import com.example.data.model.TradingSignal
import com.example.ui.components.AssetBadge
import com.example.ui.components.ConnectionStatusBadge
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
import com.example.ui.viewmodel.MainViewModel
import java.text.DecimalFormat

@Composable
fun MarketsScreen(
    viewModel: MainViewModel,
    onSelectInstrument: (TradingSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allSignals by viewModel.allSignals.collectAsState()
    val ticks by viewModel.allTicks.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()

    var selectedAsset by remember { mutableStateOf(AssetClass.ALL) }
    var sortBy by remember { mutableStateOf("CHANGE") } // "CHANGE", "SPREAD", "NAME"

    val displayedInstruments = remember(allSignals, ticks, selectedAsset, sortBy) {
        val list = allSignals.filter {
            selectedAsset == AssetClass.ALL || it.assetClass == selectedAsset
        }
        when (sortBy) {
            "CHANGE" -> list.sortedByDescending { ticks[it.symbol]?.changePercent ?: 0.0 }
            "SPREAD" -> list.sortedBy { ticks[it.symbol]?.calculatedSpread ?: it.spread }
            "NAME" -> list.sortedBy { it.symbol }
            else -> list
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCT Market Universe",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Institutional SCT Pairs & Liquidity Spreads",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            ConnectionStatusBadge(
                diagnostics = diagnostics,
                countdownSeconds = uiState.countdownSeconds,
                onClick = { viewModel.showDiagnostics(true) }
            )
        }

        // Asset Class Filter Pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AssetClass.values()) { asset ->
                val isSelected = selectedAsset == asset
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) NeutralBlue else TerminalSurfaceElevated,
                            RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) NeutralBlue else TerminalBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedAsset = asset }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = asset.displayName,
                        color = if (isSelected) Color(0xFF090D16) else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Sort By Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Sort:", color = TextMuted, fontSize = 11.sp)
            listOf("CHANGE" to "24h Change", "SPREAD" to "Tight Spread", "NAME" to "Symbol").forEach { (key, label) ->
                val isSelected = sortBy == key
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) TerminalSurfaceElevated else TerminalDark,
                            RoundedCornerShape(6.dp)
                        )
                        .border(1.dp, if (isSelected) NeutralBlue else TerminalBorder, RoundedCornerShape(6.dp))
                        .clickable { sortBy = key }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) NeutralBlue else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurfaceElevated)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("INSTRUMENT", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.5f))
            Text("BID / ASK", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.4f))
            Text("MID / SPREAD", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.4f))
            Text("24H CHG", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.0f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }

        // Markets Rows
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(displayedInstruments, key = { it.symbol }) { signal ->
                val tick = ticks[signal.symbol]
                val midPrice = tick?.currentPrice ?: signal.currentPrice
                val bid = tick?.bid ?: (midPrice - signal.spread / 2)
                val ask = tick?.ask ?: (midPrice + signal.spread / 2)
                val spread = tick?.calculatedSpread ?: signal.spread
                val chgPct = tick?.changePercent ?: 0.0

                val priceFormatter = remember(midPrice) {
                    if (midPrice < 1.0) DecimalFormat("#,##0.0000")
                    else if (midPrice < 10.0) DecimalFormat("#,##0.000")
                    else DecimalFormat("#,##0.00")
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectInstrument(signal) }
                        .testTag("market_row_${signal.symbol}"),
                    color = TerminalDark
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Instrument
                        Column(modifier = Modifier.weight(1.5f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = signal.symbol,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .background(NeutralBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, NeutralBlue, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = signal.sctArchetype.shortLabel,
                                        color = NeutralBlue,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = signal.sctStrategyType.take(24),
                                color = TextMuted,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }

                        // Bid / Ask
                        Column(modifier = Modifier.weight(1.4f)) {
                            Text(
                                text = "B: ${priceFormatter.format(bid)}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "A: ${priceFormatter.format(ask)}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Mid / Spread
                        Column(modifier = Modifier.weight(1.4f)) {
                            Text(
                                text = priceFormatter.format(midPrice),
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Sprd: ${String.format("%.4f", spread)}",
                                color = GoldAccent,
                                fontSize = 10.sp
                            )
                        }

                        // Change %
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .background(
                                    if (chgPct >= 0) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${if (chgPct >= 0) "+" else ""}${String.format("%.2f", chgPct)}%",
                                color = if (chgPct >= 0) BullishGreen else BearishRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Quick cTrader order trigger
                        IconButton(
                            onClick = { viewModel.openCTraderOrder(signal) },
                            modifier = Modifier.testTag("market_ctrader_${signal.symbol}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = "Trade on cTrader",
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Quick DXtrade order trigger
                        IconButton(
                            onClick = { viewModel.openDxTradeOrder(signal) },
                            modifier = Modifier.testTag("market_trade_${signal.symbol}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Trade on DXtrade",
                                tint = NeutralBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(TerminalBorder.copy(alpha = 0.3f))
                )
            }
        }
    }
}
