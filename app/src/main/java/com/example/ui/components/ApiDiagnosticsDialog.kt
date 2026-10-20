package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.ApiDiagnostics
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NeutralBlue
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurfaceCard
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun ApiDiagnosticsDialog(
    diagnostics: ApiDiagnostics,
    onTestPing: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TerminalBorder, RoundedCornerShape(20.dp))
                .testTag("api_diagnostics_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = TerminalSurfaceCard
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
                    Text(
                        text = "Biquote API Status",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Diagnostic Cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalSurfaceElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DiagItem(
                        label = "Base Endpoint:",
                        value = diagnostics.endpointUrl,
                        valueColor = NeutralBlue
                    )
                    DiagItem(
                        label = "Feed Mode:",
                        value = if (diagnostics.isLive) "Real-time Live Tick Stream" else "Resilient Cache Standby",
                        valueColor = if (diagnostics.isLive) BullishGreen else WarningAmber
                    )
                    DiagItem(
                        label = "Round-Trip Latency:",
                        value = "${diagnostics.latencyMs} ms",
                        valueColor = if (diagnostics.latencyMs < 300) BullishGreen else WarningAmber
                    )
                    DiagItem(
                        label = "Successful Ticks:",
                        value = "${diagnostics.successfulCalls} requests",
                        valueColor = BullishGreen
                    )
                    DiagItem(
                        label = "Failed / Timed Out:",
                        value = "${diagnostics.failedCalls} requests",
                        valueColor = if (diagnostics.failedCalls > 0) BearishRed else TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = diagnostics.statusMessage,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onTestPing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ping_biquote_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeutralBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("Ping Biquote API Now", color = com.example.ui.theme.TextOnAccent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DiagItem(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
