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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketMover
import com.example.data.model.TradingSignal
import com.example.ui.components.AssetBadge
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
fun MoversScreen(
    viewModel: MainViewModel,
    onSelectInstrument: (TradingSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    val gainers by viewModel.gainers.collectAsState()
    val losers by viewModel.losers.collectAsState()
    val mostActive by viewModel.mostActive.collectAsState()
    val marketSummary by viewModel.marketSummary.collectAsState()
    val allSignals by viewModel.allSignals.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalDark),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Market Pulse Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp))
                    .testTag("market_pulse_card"),
                shape = RoundedCornerShape(16.dp),
                color = TerminalSurfaceCard
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCT Session Pulse & Expansion",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(BullishGreen.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = marketSummary.marketSentiment,
                                color = BullishGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PulseStat(label = "Advancing", count = "${marketSummary.gainersCount}", color = BullishGreen)
                        PulseStat(label = "Declining", count = "${marketSummary.losersCount}", color = BearishRed)
                        PulseStat(label = "Neutral", count = "${marketSummary.unchangedCount}", color = NeutralBlue)
                        PulseStat(label = "Top Sector", count = "Crypto", color = GoldAccent)
                    }
                }
            }
        }

        // Top Gainers Section
        item {
            SectionHeader(title = "Top Gainers", icon = Icons.Default.ArrowUpward, tint = BullishGreen)
        }

        items(gainers, key = { "gainer_${it.symbol}" }) { mover ->
            MoverCard(mover = mover, isGainer = true) {
                allSignals.find { it.symbol == mover.symbol }?.let { onSelectInstrument(it) }
            }
        }

        // Top Losers Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "Top Losers", icon = Icons.Default.ArrowDownward, tint = BearishRed)
        }

        items(losers, key = { "loser_${it.symbol}" }) { mover ->
            MoverCard(mover = mover, isGainer = false) {
                allSignals.find { it.symbol == mover.symbol }?.let { onSelectInstrument(it) }
            }
        }

        // Most Active Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "Most Active by Volume", icon = Icons.Default.FlashOn, tint = GoldAccent)
        }

        items(mostActive, key = { "active_${it.symbol}" }) { mover ->
            MoverCard(mover = mover, isGainer = mover.changePercent >= 0) {
                allSignals.find { it.symbol == mover.symbol }?.let { onSelectInstrument(it) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MoverCard(mover: MarketMover, isGainer: Boolean, onClick: () -> Unit) {
    val formatter = remember(mover.price) {
        if (mover.price < 1.0) DecimalFormat("#,##0.0000")
        else if (mover.price < 10.0) DecimalFormat("#,##0.000")
        else DecimalFormat("#,##0.00")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("mover_card_${mover.symbol}"),
        shape = RoundedCornerShape(12.dp),
        color = TerminalSurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = mover.symbol,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    AssetBadge(assetClass = mover.assetClass)
                }
                Text(
                    text = mover.name,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = formatter.format(mover.price),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Box(
                    modifier = Modifier
                        .background(
                            if (isGainer) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${if (mover.changePercent >= 0) "+" else ""}${String.format("%.2f", mover.changePercent)}%",
                        color = if (isGainer) BullishGreen else BearishRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun PulseStat(label: String, count: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextMuted, fontSize = 10.sp)
        Text(text = count, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
