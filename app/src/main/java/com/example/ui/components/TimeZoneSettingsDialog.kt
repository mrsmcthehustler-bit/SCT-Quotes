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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.SctSettings
import com.example.data.model.formatHarareTime
import com.example.data.model.isHarareSignalWindowActive
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeutralBlue
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurfaceCard
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun TimeZoneSettingsDialog(
    currentSettings: SctSettings,
    onSave: (SctSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var autoGmt by remember(currentSettings) { mutableStateOf(currentSettings.autoDetectGmt) }
    var manualGmt by remember(currentSettings) { mutableIntStateOf(currentSettings.manualGmtOffset) }
    var asianStart by remember(currentSettings) { mutableIntStateOf(currentSettings.asianStartHour) }
    var asianEnd by remember(currentSettings) { mutableIntStateOf(currentSettings.asianEndHour) }
    var baseUtc by remember(currentSettings) { mutableIntStateOf(currentSettings.baseUtcStart) }

    // Live clock ticker
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val effectiveOffset = if (autoGmt) {
        val raw = TimeZone.getDefault().rawOffset / (1000 * 60 * 60)
        raw.coerceIn(-12, 14)
    } else {
        manualGmt
    }

    val brokerHour = ((Date(currentTimeMs).hours + (effectiveOffset - (TimeZone.getDefault().rawOffset / (1000 * 60 * 60)))) % 24 + 24) % 24

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TerminalBorder, RoundedCornerShape(20.dp))
                .testTag("time_zone_settings_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = TerminalSurfaceCard
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = NeutralBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Time Zone & Session Hours",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "SCT Theory Broker Server Time Alignment",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_tz_dialog")) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }
                }

                // Live Broker Clock Display
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
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "BROKER SERVER TIME",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val timeFormat = remember(effectiveOffset) { SimpleDateFormat("HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("GMT${if (effectiveOffset >= 0) "+$effectiveOffset" else "$effectiveOffset"}") } }
                                Text(
                                    text = timeFormat.format(Date(currentTimeMs)),
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(NeutralBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, NeutralBlue, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (effectiveOffset >= 0) "GMT+$effectiveOffset" else "GMT$effectiveOffset",
                                    color = NeutralBlue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Dedicated Harare (CAT - GMT+2) Signal Clock Card
                item {
                    val isHarareActive = currentSettings.isHarareSignalWindowActive(currentTimeMs)
                    val harareTimeStr = currentSettings.formatHarareTime(currentTimeMs)
                    val cardBorder = if (isHarareActive) BullishGreen else GoldAccent.copy(alpha = 0.6f)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.2.dp, cardBorder, RoundedCornerShape(12.dp))
                            .testTag("harare_clock_card"),
                        color = if (isHarareActive) Color(0xFF14532D).copy(alpha = 0.25f) else TerminalSurfaceElevated,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(if (isHarareActive) BullishGreen else WarningAmber, CircleShape)
                                        )
                                        Text(
                                            text = "HARARE TIME (CAT • GMT+2)",
                                            color = if (isHarareActive) BullishGreen else GoldAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Text(
                                        text = harareTimeStr,
                                        color = TextPrimary,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isHarareActive) BullishGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.15f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isHarareActive) BullishGreen else WarningAmber,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (isHarareActive) "9-10 AM ACTIVE" else "09:00 AM PENDING",
                                        color = if (isHarareActive) BullishGreen else WarningAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All USD Mode pairs are locked & dependent on the 09:00 - 10:00 AM Harare CAT signal window.",
                                color = TextMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Auto-detect GMT Toggle
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
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-detect GMT Offset",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Detects your device's local system offset automatically",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = autoGmt,
                                onCheckedChange = { autoGmt = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeutralBlue,
                                    checkedTrackColor = NeutralBlue.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.testTag("switch_auto_gmt")
                            )
                        }
                    }
                }

                // Broker Presets
                item {
                    Text(
                        text = "POPULAR BROKER SERVER PRESETS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            Triple("Harare / CAT (SCT USD Trigger)", 2, "Central Africa Time — Harare 9:00 - 10:00 AM (GMT+2)"),
                            Triple("FTMO / IC Markets / Pepperstone", 2, "Standard MetaTrader Server (GMT+2 / +3 DST)"),
                            Triple("London / UTC Standard", 0, "Western Europe & UK (GMT+0)"),
                            Triple("New York / EST", -4, "US Eastern Timezone (GMT-4 / -5)"),
                            Triple("Tokyo / JST", 9, "Japan Standard Asian Time (GMT+9)"),
                            Triple("Singapore / Hong Kong", 8, "Asian Financial Hub (GMT+8)")
                        ).forEach { (name, offset, desc) ->
                            val isSelected = !autoGmt && manualGmt == offset
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isSelected) NeutralBlue else TerminalBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        autoGmt = false
                                        manualGmt = offset
                                    }
                                    .testTag("preset_tz_$offset"),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NeutralBlue.copy(alpha = 0.15f) else TerminalSurfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(desc, color = TextMuted, fontSize = 10.sp)
                                    }
                                    Text(
                                        text = if (offset >= 0) "GMT+$offset" else "GMT$offset",
                                        color = if (isSelected) NeutralBlue else GoldAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Manual GMT Offset Slider (if Auto is disabled)
                if (!autoGmt) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Manual GMT Offset (InpManualGMTOffset)", color = TextPrimary, fontSize = 12.sp)
                                Text(
                                    if (manualGmt >= 0) "GMT+$manualGmt" else "GMT$manualGmt",
                                    color = NeutralBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Slider(
                                value = manualGmt.toFloat(),
                                onValueChange = { manualGmt = it.toInt() },
                                valueRange = -12f..14f,
                                steps = 25,
                                colors = SliderDefaults.colors(thumbColor = NeutralBlue, activeTrackColor = NeutralBlue),
                                modifier = Modifier.testTag("slider_manual_gmt")
                            )
                        }
                    }
                }

                // Strategy Session Hour Inputs
                item {
                    Text(
                        text = "SCT SESSION HOURS (BROKER LOCAL TIME)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp)),
                        color = TerminalSurfaceElevated,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Asian Range Window", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Evaluates Asian High/Low boundaries", color = TextMuted, fontSize = 10.sp)
                                }
                                Text(
                                    text = String.format("%02d:00 - %02d:00 Broker", asianStart, asianEnd),
                                    color = Color(0xFF9333EA),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Base Strategy Start Hour", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    val adjustedHour = ((baseUtc + effectiveOffset) % 24 + 24) % 24
                                    Text("Adjusts to $adjustedHour:00 Broker Server Time", color = NeutralBlue, fontSize = 10.sp)
                                }
                                Text(
                                    text = String.format("%02d:00 UTC", baseUtc),
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // World Trading Sessions Radar
                item {
                    Text(
                        text = "WORLD MARKET SESSIONS RADAR",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val utcHour = ((Date(currentTimeMs).hours - (TimeZone.getDefault().rawOffset / (1000 * 60 * 60))) % 24 + 24) % 24
                    val isTokyoOpen = utcHour in 0..9
                    val isLondonOpen = utcHour in 8..16
                    val isNewYorkOpen = utcHour in 13..21
                    val isSydneyOpen = utcHour >= 21 || utcHour <= 6

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SessionPill(name = "Tokyo", isOpen = isTokyoOpen, modifier = Modifier.weight(1f))
                        SessionPill(name = "London", isOpen = isLondonOpen, modifier = Modifier.weight(1f))
                        SessionPill(name = "New York", isOpen = isNewYorkOpen, modifier = Modifier.weight(1f))
                        SessionPill(name = "Sydney", isOpen = isSydneyOpen, modifier = Modifier.weight(1f))
                    }
                }

                // Apply Button
                item {
                    Button(
                        onClick = {
                            val updated = currentSettings.copy(
                                autoDetectGmt = autoGmt,
                                manualGmtOffset = manualGmt,
                                asianStartHour = asianStart,
                                asianEndHour = asianEnd,
                                baseUtcStart = baseUtc
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apply_tz_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeutralBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Apply Time Zone & Realign",
                            color = Color(0xFF090D16),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionPill(name: String, isOpen: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .border(
                1.dp,
                if (isOpen) BullishGreen.copy(alpha = 0.5f) else TerminalBorder,
                RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (isOpen) Color(0xFF14532D).copy(alpha = 0.35f) else TerminalSurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(name, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(if (isOpen) BullishGreen else TextMuted, CircleShape)
                )
                Text(
                    text = if (isOpen) "OPEN" else "CLOSED",
                    color = if (isOpen) BullishGreen else TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
