package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetClass
import com.example.data.model.TradingSignal
import com.example.ui.components.AssetBadge
import com.example.ui.components.DaySetupItemCard
import com.example.ui.components.SctChassisDashboard
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
fun FiveDaySetupScreen(
    viewModel: MainViewModel,
    onOpenSignalDetail: (TradingSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val performance by viewModel.fiveDayPerformance.collectAsState()
    val selectedSymbol by viewModel.selectedPerformanceSymbol.collectAsState()
    val allSignals by viewModel.allSignals.collectAsState()
    val ticks by viewModel.allTicks.collectAsState()

    var showPairSelectorDialog by remember { mutableStateOf(false) }

    val quickSymbols = listOf(
        "EURUSD", "GBPUSD", "USDJPY", "USDCAD",
        "BTCUSD", "ETHUSD", "XAUUSD", "AUDUSD", "SPX500"
    )

    val currentTick = ticks[selectedSymbol]
    val currentPrice = currentTick?.currentPrice ?: performance.currentPrice
    val currentSpread = currentTick?.calculatedSpread ?: 0.0

    val priceFormat = remember(selectedSymbol, currentPrice) {
        if (currentPrice < 1.0) DecimalFormat("#,##0.0000")
        else if (currentPrice < 10.0) DecimalFormat("#,##0.000")
        else if (selectedSymbol.contains("JPY")) DecimalFormat("#,##0.000")
        else DecimalFormat("#,##0.00")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalDark)
            .testTag("five_day_setup_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "5-Day Setup Performance",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "SCT Theory Institutional Strategy History",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Copy Performance Report Button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val sb = StringBuilder()
                            sb.appendLine("=== SCT THEORY 5-DAY PERFORMANCE REPORT ===")
                            sb.appendLine("Pair: ${performance.symbol} (${performance.name})")
                            sb.appendLine("Mode: ${performance.mode}")
                            sb.appendLine("Win Rate: ${performance.winRatePercent}% (${performance.totalWins}W / ${performance.totalLosses}L)")
                            sb.appendLine("Net Pips: ${if (performance.netPips >= 0) "+" else ""}${performance.netPips} pips")
                            sb.appendLine("Avg Pips/Day: ${performance.avgPipsPerDay} pips/day")
                            sb.appendLine("Profit Factor: ${performance.profitFactor}")
                            sb.appendLine("--- Daily Breakdown ---")
                            performance.dailySetups.forEach { day ->
                                sb.appendLine("Day ${day.dayNumber} (${day.fullDate}): ${day.status} | ${if (day.pips >= 0) "+" else ""}${day.pips}p | Entry: ${day.entryPrice} | TP: ${day.takeProfit} | SL: ${day.stopLoss}")
                            }
                            clipboard.setPrimaryClip(ClipData.newPlainText("SCT Performance Report", sb.toString()))
                            viewModel.setToast("5-Day Performance report copied to clipboard!")
                        },
                        modifier = Modifier.testTag("copy_performance_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Report",
                            tint = TextSecondary
                        )
                    }

                    // Pair picker button
                    IconButton(
                        onClick = { showPairSelectorDialog = true },
                        modifier = Modifier.testTag("open_pair_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Select Any Pair",
                            tint = NeutralBlue
                        )
                    }
                }
            }
        }

        // Quick Pair Selector Carousel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Instrument Pair:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier
                            .clickable { showPairSelectorDialog = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Browse All Pairs",
                            color = NeutralBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = NeutralBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickSymbols) { sym ->
                        val isSelected = sym == selectedSymbol
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) NeutralBlue else TerminalSurfaceElevated,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NeutralBlue else TerminalBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.selectPerformanceSymbol(sym) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("select_pair_$sym"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sym,
                                color = if (isSelected) Color(0xFF090D16) else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Active Selected Pair Hero Bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp)),
                color = TerminalSurfaceElevated,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = performance.symbol,
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            AssetBadge(assetClass = performance.assetClass)
                        }
                        Text(
                            text = performance.name,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = priceFormat.format(currentPrice),
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Sprd: ${String.format("%.4f", currentSpread)}",
                                color = GoldAccent,
                                fontSize = 10.sp
                            )

                            // Quick button to view live signal detail
                            val signal = allSignals.find { it.symbol == selectedSymbol }
                            if (signal != null) {
                                Row(
                                    modifier = Modifier
                                        .clickable { onOpenSignalDetail(signal) }
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = NeutralBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("View Live", color = NeutralBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // SCT EA Chassis Dashboard Panel
        item {
            SctChassisDashboard(performance = performance)
        }

        // Daily Breakdown Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "5 Previous Trading Days Breakdown",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap day to view levels",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        // 5 Days Setup Performance Cards
        items(performance.dailySetups, key = { "${it.dayNumber}_${it.fullDate}" }) { daySetup ->
            DaySetupItemCard(
                daySetup = daySetup,
                symbol = performance.symbol
            )
        }

        // SCT Theory Explanation Footer Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp)),
                color = TerminalSurfaceCard,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "❖ SCT Theory Strategy Methodology",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Asian Session (00:00 - 06:00): Establishes the initial overnight liquidity range.\n" +
                                "• London Overlap (09:00 UTC): Institutional open triggers breakout of Asian High/Low.\n" +
                                "• Retest & Pocket Entries: EURUSD requires Asian level retest targeting PDH/PDL; GBPUSD utilizes EMA 9/21 pocket bounces; USDJPY relies on 61.8% Fibonacci retracement.\n" +
                                "• Standardized Risk:Reward: Setups target 1:1.5 minimum R:R with fixed stop losses behind structural session boundaries.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // Modal Dialog to Select ANY pair across all supported instruments
    if (showPairSelectorDialog) {
        PairSelectorDialog(
            viewModel = viewModel,
            currentSelectedSymbol = selectedSymbol,
            onSelectPair = { sym ->
                viewModel.selectPerformanceSymbol(sym)
                showPairSelectorDialog = false
            },
            onDismiss = { showPairSelectorDialog = false }
        )
    }
}

@Composable
private fun PairSelectorDialog(
    viewModel: MainViewModel,
    currentSelectedSymbol: String,
    onSelectPair: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedAssetClass by remember { mutableStateOf(AssetClass.ALL) }

    val supported = viewModel.repository.supportedInstruments
    val ticks by viewModel.allTicks.collectAsState()

    val filteredList = remember(searchQuery, selectedAssetClass, supported) {
        supported.filter { item ->
            val matchesAsset = selectedAssetClass == AssetClass.ALL || item.assetClass == selectedAssetClass
            val matchesSearch = searchQuery.isEmpty() ||
                    item.symbol.contains(searchQuery, ignoreCase = true) ||
                    item.name.contains(searchQuery, ignoreCase = true)
            matchesAsset && matchesSearch
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Pair for 5-Day Setup",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search EURUSD, BTC, Gold, SPX…", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeutralBlue,
                        unfocusedBorderColor = TerminalBorder,
                        focusedContainerColor = TerminalSurfaceElevated,
                        unfocusedContainerColor = TerminalSurfaceElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pair_selector_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Asset Class Filter Pills
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(AssetClass.values()) { asset ->
                        val isSelected = selectedAssetClass == asset
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) NeutralBlue else TerminalSurfaceElevated,
                                    RoundedCornerShape(16.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NeutralBlue else TerminalBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedAssetClass = asset }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = asset.displayName,
                                color = if (isSelected) Color(0xFF090D16) else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Instrument List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredList, key = { it.symbol }) { info ->
                        val isSelected = info.symbol == currentSelectedSymbol
                        val tick = ticks[info.symbol]
                        val price = tick?.currentPrice ?: info.defaultPrice

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isSelected) NeutralBlue else TerminalBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectPair(info.symbol) }
                                .testTag("pair_option_${info.symbol}"),
                            color = if (isSelected) NeutralBlue.copy(alpha = 0.15f) else TerminalSurfaceElevated,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = info.symbol,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        AssetBadge(assetClass = info.assetClass)
                                    }
                                    Text(
                                        text = info.name,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (price < 1.0) String.format("%.4f", price)
                                        else if (price < 10.0) String.format("%.3f", price)
                                        else String.format("%.2f", price),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "SELECTED",
                                            color = BullishGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NeutralBlue, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = TerminalDark,
        shape = RoundedCornerShape(16.dp)
    )
}
