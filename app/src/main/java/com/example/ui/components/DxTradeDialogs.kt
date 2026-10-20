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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DxTradeAccount
import com.example.data.model.DxTradeConnectionState
import com.example.data.model.DxTradeCredentials
import com.example.data.model.DxTradeOrderRequest
import com.example.data.model.DxTradePosition
import com.example.data.model.TradingSignal
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DxTradeConnectDialog(
    connectionState: DxTradeConnectionState,
    account: DxTradeAccount?,
    savedCredentials: DxTradeCredentials,
    onConnect: (DxTradeCredentials) -> Unit,
    onConnectDemo: () -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    var serverUrl by remember { mutableStateOf(savedCredentials.serverUrl) }
    var username by remember { mutableStateOf(savedCredentials.username) }
    var password by remember { mutableStateOf(savedCredentials.password) }
    var accountId by remember { mutableStateOf(savedCredentials.accountId) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isConnected = connectionState == DxTradeConnectionState.CONNECTED && account != null

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dxtrade_connect_dialog")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF0284C7).copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, Color(0xFF38BDF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "DXtrade",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "DXtrade REST API",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Institutional Broker Integration",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_dxtrade_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isConnected && account != null) {
                    // Connected Status View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurfaceElevated, RoundedCornerShape(12.dp))
                            .border(1.dp, BullishGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(BullishGreen, CircleShape)
                                    )
                                    Text(
                                        text = "LIVE SESSION CONNECTED",
                                        color = BullishGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = account.account,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Account Balance", color = TextMuted, fontSize = 10.sp)
                                    Text(
                                        text = "$${DecimalFormat("#,##0.00").format(account.balance)}",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Equity", color = TextMuted, fontSize = 10.sp)
                                    Text(
                                        text = "$${DecimalFormat("#,##0.00").format(account.equity)}",
                                        color = BullishGreen,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Free Margin: $${DecimalFormat("#,##0.00").format(account.freeMargin)}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Leverage: 1:${account.leverage}",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onDisconnect,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dxtrade_disconnect_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BearishRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Disconnect DXtrade Session", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Credentials Form
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Broker Server URL", fontSize = 12.sp) },
                        placeholder = { Text("https://demo.dx.trade") },
                        leadingIcon = {
                            Icon(Icons.Default.Public, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalSurfaceElevated,
                            unfocusedContainerColor = TerminalSurfaceElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dxtrade_server_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username / Trader ID", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalSurfaceElevated,
                            unfocusedContainerColor = TerminalSurfaceElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dxtrade_username_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password / API Token", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalSurfaceElevated,
                            unfocusedContainerColor = TerminalSurfaceElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dxtrade_password_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = accountId,
                        onValueChange = { accountId = it },
                        label = { Text("Account Number", fontSize = 12.sp) },
                        placeholder = { Text("DEMO-888999") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalSurfaceElevated,
                            unfocusedContainerColor = TerminalSurfaceElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dxtrade_account_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Connect Button
                    Button(
                        onClick = {
                            onConnect(
                                DxTradeCredentials(
                                    serverUrl = serverUrl.trim(),
                                    username = username.trim(),
                                    password = password,
                                    accountId = accountId.trim(),
                                    isDemoMode = false
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dxtrade_connect_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = connectionState != DxTradeConnectionState.CONNECTING
                    ) {
                        if (connectionState == DxTradeConnectionState.CONNECTING) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connecting via REST API…", color = Color.White)
                        } else {
                            Text("Connect Broker Account", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1-Tap Demo Connect
                    OutlinedButton(
                        onClick = onConnectDemo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dxtrade_demo_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("1-Tap Demo Sandbox ($100k Virtual)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DxTradeOrderDialog(
    signal: TradingSignal,
    account: DxTradeAccount?,
    onExecuteOrder: (DxTradeOrderRequest) -> Unit,
    onOpenConnectDialog: () -> Unit,
    onDismiss: () -> Unit
) {
    val isBullish = signal.signalType.isBullish
    val defaultLot = 0.50
    var lotSizeStr by remember { mutableStateOf(defaultLot.toString()) }
    var orderType by remember { mutableStateOf("MARKET") } // "MARKET" or "LIMIT"
    var isSubmitted by remember { mutableStateOf(false) }

    val lot = lotSizeStr.toDoubleOrNull() ?: 0.10
    val entryPrice = signal.entryPrice
    val stopLoss = signal.stopLoss
    val takeProfit = signal.tp1

    val slDistance = Math.abs(entryPrice - stopLoss)
    val tpDistance = Math.abs(takeProfit - entryPrice)

    // Calculate USD risk & reward
    val pointMultiplier = if (signal.symbol.contains("JPY")) 100.0 else 10000.0
    val riskUsd = (slDistance * pointMultiplier * lot * 10.0)
    val rewardUsd = (tpDistance * pointMultiplier * lot * 10.0)

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dxtrade_order_dialog")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFF0284C7), RoundedCornerShape(20.dp)),
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
                                .background(Color(0xFF0284C7).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DXTRADE",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = "Trade ${signal.symbol}",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_order_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Account Connection Banner
                if (account != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Account: ${account.account}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Free Margin: $${DecimalFormat("#,##0.00").format(account.freeMargin)}",
                            color = BullishGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onOpenConnectDialog() }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DXtrade not connected. Tap to connect account.",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Connect", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Pill & Order Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isBullish) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, if (isBullish) BullishGreen else BearishRed, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isBullish) "BUY / LONG" else "SELL / SHORT",
                            color = if (isBullish) BullishGreen else BearishRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Order type pills
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("MARKET", "LIMIT").forEach { type ->
                            val isSel = orderType == type
                            Box(
                                modifier = Modifier
                                    .background(if (isSel) NeutralBlue else TerminalSurfaceElevated, RoundedCornerShape(6.dp))
                                    .clickable { orderType = type }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = type,
                                    color = if (isSel) Color(0xFF090D16) else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lot Size Input & Fast Selectors
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Volume (Lots)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "SCT Risk Mode",
                            color = Color(0xFFA78BFA),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = lotSizeStr,
                        onValueChange = { lotSizeStr = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalSurfaceElevated,
                            unfocusedContainerColor = TerminalSurfaceElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dxtrade_lots_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Lot Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.01, 0.10, 0.50, 1.00).forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(TerminalSurfaceElevated, RoundedCornerShape(6.dp))
                                    .border(0.5.dp, TerminalBorder, RoundedCornerShape(6.dp))
                                    .clickable { lotSizeStr = preset.toString() }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.toString(),
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Levels Matrix: Entry, SL, TP
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Execution Entry:", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = String.format("%.4f", entryPrice),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Stop Loss (SL):", color = BearishRed, fontSize = 11.sp)
                            Text(
                                text = String.format("%.4f", stopLoss),
                                color = BearishRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Take Profit (TP1):", color = BullishGreen, fontSize = 11.sp)
                            Text(
                                text = String.format("%.4f", takeProfit),
                                color = BullishGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Execute Button
                Button(
                    onClick = {
                        isSubmitted = true
                        onExecuteOrder(
                            DxTradeOrderRequest(
                                account = account?.account ?: "DEMO-888999",
                                symbol = signal.symbol,
                                side = if (isBullish) "BUY" else "SELL",
                                orderType = orderType,
                                quantity = lot,
                                price = entryPrice,
                                stopLoss = stopLoss,
                                takeProfit = takeProfit,
                                comment = "SCT ${signal.sctStrategyType}"
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_dxtrade_order_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBullish) BullishGreen else BearishRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.White)
                        Text(
                            text = "Send ${if (isBullish) "BUY" else "SELL"} Order to DXtrade",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DxTradePositionsDialog(
    positions: List<DxTradePosition>,
    account: DxTradeAccount?,
    onClosePosition: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val totalPl = positions.sumOf { it.pl }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dxtrade_positions_dialog")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFF0284C7), RoundedCornerShape(20.dp)),
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
                    Column {
                        Text(
                            text = "DXtrade Live Positions",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Active Prop / Broker Trades (${positions.size})",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_positions_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Summary bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Floating Net P&L:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = "${if (totalPl >= 0) "+$" else "-$"}${DecimalFormat("#,##0.00").format(Math.abs(totalPl))}",
                        color = if (totalPl >= 0) BullishGreen else BearishRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (positions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No open positions on DXtrade account.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(positions, key = { it.positionId }) { pos ->
                            val isBull = pos.side.equals("BUY", ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, TerminalBorder, RoundedCornerShape(10.dp)),
                                color = TerminalSurfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(pos.symbol, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (isBull) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "${pos.side} ${pos.quantity}L",
                                                    color = if (isBull) BullishGreen else BearishRed,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Open: ${pos.openPrice}  Now: ${pos.currentPrice}",
                                            color = TextMuted,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${if (pos.pl >= 0) "+$" else "-$"}${DecimalFormat("#,##0.00").format(Math.abs(pos.pl))}",
                                            color = if (pos.pl >= 0) BullishGreen else BearishRed,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        Button(
                                            onClick = { onClosePosition(pos.positionId) },
                                            modifier = Modifier.height(28.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BearishRed.copy(alpha = 0.85f)),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Close", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
