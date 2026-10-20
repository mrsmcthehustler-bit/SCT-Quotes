package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.NinjaSettings
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NinjaSettingsDialog(
    initialSettings: NinjaSettings,
    onSaveSettings: (NinjaSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var isNinjaEnabled by remember { mutableStateOf(initialSettings.enabled) }
    var tradeZoneBreakouts by remember { mutableStateOf(initialSettings.tradeZoneBreakouts) }
    var riskPercentStr by remember { mutableStateOf(initialSettings.riskPercent.toString()) }
    var targetPercentStr by remember { mutableStateOf(initialSettings.targetPercent.toString()) }
    var slBufferPointsStr by remember { mutableStateOf(initialSettings.slBufferPoints.toString()) }
    var requiredClosesStr by remember { mutableStateOf(initialSettings.requiredCloses.toString()) }
    var swingLookbackStr by remember { mutableStateOf(initialSettings.swingLookback.toString()) }
    var enableTPHunter by remember { mutableStateOf(initialSettings.enableTPHunter) }
    var tpFractalLookbackStr by remember { mutableStateOf(initialSettings.tpFractalLookback.toString()) }
    var oneTradePerDay by remember { mutableStateOf(initialSettings.oneTradePerDay) }
    var enablePartialTP by remember { mutableStateOf(initialSettings.enablePartialTP) }
    var partialTPRStr by remember { mutableStateOf(initialSettings.partialTPR.toString()) }
    var partialTPPercentStr by remember { mutableStateOf(initialSettings.partialTPPercent.toString()) }
    var moveToBreakEven by remember { mutableStateOf(initialSettings.moveToBreakEven) }
    var beBufferPointsStr by remember { mutableStateOf(initialSettings.beBufferPoints.toString()) }
    var enablePyramiding by remember { mutableStateOf(initialSettings.enablePyramiding) }
    var magicNumberStr by remember { mutableStateOf(initialSettings.magicNumber.toString()) }

    val scrollState = rememberScrollState()

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("ninja_settings_dialog")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp)),
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
                                .background(GoldAccent.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, GoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🥷",
                                fontSize = 16.sp
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Ninja",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                                Box(
                                    modifier = Modifier
                                        .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text("USDJPY EXCLUSIVE", color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "SCT Theory Fixed V2.80 Engine",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_ninja_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The Master "Ninja" Option Switch
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (isNinjaEnabled) GoldAccent else TerminalBorder,
                            RoundedCornerShape(12.dp)
                        ),
                    color = TerminalSurfaceElevated
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ninja (USDJPY Mode)",
                                color = if (isNinjaEnabled) GoldAccent else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Focus purely on USDJPY with institutional SC Zone, Order Blocks, and Safety Mode rules.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isNinjaEnabled,
                            onCheckedChange = { isNinjaEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF090D16),
                                checkedTrackColor = GoldAccent,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = TerminalSurfaceElevated
                            ),
                            modifier = Modifier.testTag("ninja_master_switch")
                        )
                    }
                }

                // Settings for Ninja ONLY appear if user switches Ninja ON!
                AnimatedVisibility(
                    visible = isNinjaEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .padding(top = 12.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Section 1: SCT Risk & Strategy
                        SectionHeader(title = "SCT Strategy & Execution Rules")

                        NinjaSwitchRow(
                            title = "Safety Mode (1 Trade / Day)",
                            subtitle = "Enforce institutional daily limit of 1 trade per day.",
                            checked = oneTradePerDay,
                            onCheckedChange = { oneTradePerDay = it },
                            testTag = "ninja_safety_mode_switch"
                        )

                        NinjaSwitchRow(
                            title = "Direct Zone Breakouts",
                            subtitle = "Execute immediately on SC Zone break (off = wait for M15 Order Block).",
                            checked = tradeZoneBreakouts,
                            onCheckedChange = { tradeZoneBreakouts = it },
                            testTag = "ninja_breakouts_switch"
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NinjaInputField(
                                label = "Base Risk %",
                                value = riskPercentStr,
                                onValueChange = { riskPercentStr = it },
                                modifier = Modifier.weight(1f)
                            )
                            NinjaInputField(
                                label = "Fallback Target %",
                                value = targetPercentStr,
                                onValueChange = { targetPercentStr = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NinjaInputField(
                                label = "SL Buffer (Pts)",
                                value = slBufferPointsStr,
                                onValueChange = { slBufferPointsStr = it },
                                modifier = Modifier.weight(1f)
                            )
                            NinjaInputField(
                                label = "M5 Closes Req.",
                                value = requiredClosesStr,
                                onValueChange = { requiredClosesStr = it },
                                modifier = Modifier.weight(1f)
                            )
                            NinjaInputField(
                                label = "Swing Lookback",
                                value = swingLookbackStr,
                                onValueChange = { swingLookbackStr = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Section 2: TP Hunter
                        SectionHeader(title = "TP Hunter Settings (Fractal Targets)")

                        NinjaSwitchRow(
                            title = "Enable TP Hunter",
                            subtitle = "Dynamically target recent M15 fractal highs and lows.",
                            checked = enableTPHunter,
                            onCheckedChange = { enableTPHunter = it },
                            testTag = "ninja_tp_hunter_switch"
                        )

                        if (enableTPHunter) {
                            NinjaInputField(
                                label = "Fractal Lookback Bars",
                                value = tpFractalLookbackStr,
                                onValueChange = { tpFractalLookbackStr = it }
                            )
                        }

                        // Section 3: Profit Maximizer
                        SectionHeader(title = "Profit Maximizer (Partial TP & BE)")

                        NinjaSwitchRow(
                            title = "Partial Take Profit",
                            subtitle = "Secure gains automatically before reaching final target.",
                            checked = enablePartialTP,
                            onCheckedChange = { enablePartialTP = it },
                            testTag = "ninja_partial_tp_switch"
                        )

                        if (enablePartialTP) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                NinjaInputField(
                                    label = "Partial TP (R-Multiple)",
                                    value = partialTPRStr,
                                    onValueChange = { partialTPRStr = it },
                                    modifier = Modifier.weight(1f)
                                )
                                NinjaInputField(
                                    label = "Close Volume (%)",
                                    value = partialTPPercentStr,
                                    onValueChange = { partialTPPercentStr = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        NinjaSwitchRow(
                            title = "Move SL to Break-Even",
                            subtitle = "Eliminate trade risk when TP1 is secured.",
                            checked = moveToBreakEven,
                            onCheckedChange = { moveToBreakEven = it },
                            testTag = "ninja_move_be_switch"
                        )

                        if (moveToBreakEven) {
                            NinjaInputField(
                                label = "BE Buffer Points",
                                value = beBufferPointsStr,
                                onValueChange = { beBufferPointsStr = it }
                            )
                        }

                        NinjaSwitchRow(
                            title = "Pyramiding Scale-In",
                            subtitle = "Add up to 3 scaled entries into winning trends.",
                            checked = enablePyramiding,
                            onCheckedChange = { enablePyramiding = it },
                            testTag = "ninja_pyramiding_switch"
                        )

                        // Section 4: Magic Number
                        SectionHeader(title = "EA Magic Number Tracking")

                        NinjaInputField(
                            label = "Magic Number",
                            value = magicNumberStr,
                            onValueChange = { magicNumberStr = it }
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                if (!isNinjaEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Settings for Ninja will only appear if user switches Ninja on.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val def = NinjaSettings()
                            isNinjaEnabled = def.enabled
                            tradeZoneBreakouts = def.tradeZoneBreakouts
                            riskPercentStr = def.riskPercent.toString()
                            targetPercentStr = def.targetPercent.toString()
                            slBufferPointsStr = def.slBufferPoints.toString()
                            requiredClosesStr = def.requiredCloses.toString()
                            swingLookbackStr = def.swingLookback.toString()
                            enableTPHunter = def.enableTPHunter
                            tpFractalLookbackStr = def.tpFractalLookback.toString()
                            oneTradePerDay = def.oneTradePerDay
                            enablePartialTP = def.enablePartialTP
                            partialTPRStr = def.partialTPR.toString()
                            partialTPPercentStr = def.partialTPPercent.toString()
                            moveToBreakEven = def.moveToBreakEven
                            beBufferPointsStr = def.beBufferPoints.toString()
                            enablePyramiding = def.enablePyramiding
                            magicNumberStr = def.magicNumber.toString()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reset V2.80", color = TextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val saved = NinjaSettings(
                                enabled = isNinjaEnabled,
                                tradeZoneBreakouts = tradeZoneBreakouts,
                                riskPercent = riskPercentStr.toDoubleOrNull() ?: 0.5,
                                targetPercent = targetPercentStr.toDoubleOrNull() ?: 1.5,
                                slBufferPoints = slBufferPointsStr.toIntOrNull() ?: 20,
                                requiredCloses = requiredClosesStr.toIntOrNull() ?: 2,
                                swingLookback = swingLookbackStr.toIntOrNull() ?: 10,
                                enableTPHunter = enableTPHunter,
                                tpFractalLookback = tpFractalLookbackStr.toIntOrNull() ?: 20,
                                oneTradePerDay = oneTradePerDay,
                                enablePartialTP = enablePartialTP,
                                partialTPR = partialTPRStr.toDoubleOrNull() ?: 1.5,
                                partialTPPercent = partialTPPercentStr.toDoubleOrNull() ?: 50.0,
                                moveToBreakEven = moveToBreakEven,
                                beBufferPoints = beBufferPointsStr.toIntOrNull() ?: 10,
                                enablePyramiding = enablePyramiding,
                                magicNumber = magicNumberStr.toLongOrNull() ?: 888999L
                            )
                            onSaveSettings(saved)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_ninja_settings_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Ninja Settings", color = Color(0xFF090D16), fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = Color(0xFFA78BFA),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun NinjaSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = TerminalSurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextMuted, fontSize = 10.sp, lineHeight = 13.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF090D16),
                    checkedTrackColor = GoldAccent,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = TerminalSurfaceCard
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
private fun NinjaInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = GoldAccent,
            unfocusedBorderColor = TerminalBorder,
            focusedContainerColor = TerminalSurfaceElevated,
            unfocusedContainerColor = TerminalSurfaceElevated
        ),
        modifier = modifier.fillMaxWidth()
    )
}
