package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DaySetupPerformance
import com.example.data.model.PairFiveDayPerformance
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

@Composable
fun SctChassisDashboard(
    performance: PairFiveDayPerformance,
    modifier: Modifier = Modifier
) {
    val isProfitable = performance.netPips >= 0
    val netColor = if (isProfitable) BullishGreen else BearishRed

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp))
            .testTag("sct_chassis_dashboard"),
        color = TerminalSurfaceCard,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Bar
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
                            .size(10.dp)
                            .background(BullishGreen, CircleShape)
                    )
                    Text(
                        text = "❖ SCT THEORY INSTITUTIONAL",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(TerminalSurfaceElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "v5.40 EA ENGINE",
                        color = GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Operational Mode Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STRATEGY EXECUTION MODE",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = performance.mode,
                            color = NeutralBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "R:R 1:1.5 Target",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Stat Grid: Win Rate, Net Pips, Avg/Day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Win Rate Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(TerminalSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("5-DAY WIN RATE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.1f", performance.winRatePercent)}%",
                        color = if (performance.winRatePercent >= 50) BullishGreen else BearishRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${performance.totalWins}W / ${performance.totalLosses}L",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                // Net Pips Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(netColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                        .border(1.dp, netColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NET PIP PROFIT", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (performance.netPips >= 0) "+" else ""}${String.format("%.1f", performance.netPips)}",
                        color = netColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Pips",
                        color = netColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Avg Pips / Day
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(TerminalSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("AVG PIPS / DAY", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (performance.avgPipsPerDay >= 0) "+" else ""}${String.format("%.1f", performance.avgPipsPerDay)}",
                        color = if (performance.avgPipsPerDay >= 0) BullishGreen else BearishRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "PF: ${String.format("%.2f", performance.profitFactor)}",
                        color = GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub Stats Bar: Pips Gained, Pips Lost, Best/Worst Day
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Gained: +${String.format("%.1f", performance.totalPipsGained)} pips",
                        color = BullishGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Lost: -${String.format("%.1f", performance.totalPipsLost)} pips",
                        color = BearishRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Best: +${String.format("%.1f", performance.bestDayPips)}p",
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DaySetupItemCard(
    daySetup: DaySetupPerformance,
    symbol: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "arrow_rotation")

    val isWin = daySetup.isWin
    val statusColor = if (isWin) BullishGreen else BearishRed
    val biasColor = if (daySetup.isLong) BullishGreen else BearishRed

    val priceFormat = remember(symbol, daySetup.entryPrice) {
        if (daySetup.entryPrice < 1.0) DecimalFormat("#,##0.0000")
        else if (daySetup.entryPrice < 10.0) DecimalFormat("#,##0.000")
        else if (symbol.contains("JPY")) DecimalFormat("#,##0.000")
        else DecimalFormat("#,##0.00")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, if (expanded) statusColor.copy(alpha = 0.5f) else TerminalBorder, RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .testTag("day_setup_card_${daySetup.dayNumber}"),
        color = TerminalSurfaceCard,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day Badge & Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(NeutralBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, NeutralBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "DAY ${daySetup.dayNumber} • ${daySetup.dayOfWeek}",
                            color = NeutralBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = daySetup.fullDate,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Win/Loss Pill & Arrow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                            .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isWin) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${if (daySetup.pips >= 0) "+" else ""}${String.format("%.1f", daySetup.pips)} pips",
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationState)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub Header: Bias and Strategy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (daySetup.isLong) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = biasColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = daySetup.bias,
                        color = biasColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = daySetup.setupStrategy,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Compact Level Strip (Entry, SL, TP)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ENTRY", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    Text(priceFormat.format(daySetup.entryPrice), color = NeutralBlue, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("TAKE PROFIT", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    Text(priceFormat.format(daySetup.takeProfit), color = BullishGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("STOP LOSS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    Text(priceFormat.format(daySetup.stopLoss), color = BearishRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("RESULT", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    Text(if (isWin) "WIN" else "LOSS", color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }

            // Expanded Detailed Section
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Session Ranges & Benchmarks Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "SCT SESSION LEVELS & TIME WINDOWS",
                                color = GoldAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Session / Asian High:", color = TextSecondary, fontSize = 11.sp)
                                Text(priceFormat.format(daySetup.sessionRangeHigh), color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Session / Asian Low:", color = TextSecondary, fontSize = 11.sp)
                                Text(priceFormat.format(daySetup.sessionRangeLow), color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Previous Day High (PDH):", color = TextSecondary, fontSize = 11.sp)
                                Text(priceFormat.format(daySetup.pdh), color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Previous Day Low (PDL):", color = TextSecondary, fontSize = 11.sp)
                                Text(priceFormat.format(daySetup.pdl), color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Triggered Time:", color = TextSecondary, fontSize = 11.sp)
                                Text(daySetup.triggerTime, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Target / Exit Time:", color = TextSecondary, fontSize = 11.sp)
                                Text(daySetup.exitTime, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Trade Rationale and Technical Notes
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NeutralBlue,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Column {
                                Text("Trade Execution Analysis", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = daySetup.notes,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
