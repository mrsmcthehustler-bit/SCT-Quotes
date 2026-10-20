package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.CTraderAccount
import com.example.data.model.CTraderAppRegistrationDetails
import com.example.data.model.CTraderAuthState
import com.example.data.model.CTraderPosition
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
import com.example.ui.theme.WarningAmber
import java.text.DecimalFormat
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CTraderTerminalDialog(
    state: CTraderAuthState,
    onSaveCredentials: (String, String, String) -> Unit = { _, _, _ -> },
    onAuthCodeSubmit: (String) -> Unit = {},
    onSelectAccount: (CTraderAccount) -> Unit = {},
    onConnectDemo: () -> Unit,
    onDisconnect: () -> Unit,
    onClosePosition: (Long) -> Unit,
    onCloseAllPositions: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (state.isAuthorized) 1 else 0) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("ctrader_terminal_dialog")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, TerminalBorder, RoundedCornerShape(20.dp)),
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
                                .background(NeutralBlue.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, NeutralBlue.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = "cTrader Open API",
                                tint = NeutralBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Spotware cTrader Open API",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (state.isAuthorized) state.statusMessage else "Institutional Trade Execution",
                                color = if (state.isAuthorized) BullishGreen else TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_ctrader_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = TerminalSurfaceElevated,
                    contentColor = NeutralBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeutralBlue,
                            height = 2.dp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(0.5.dp, TerminalBorder, RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "App Setup & Auth",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) NeutralBlue else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Trading Terminal",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) NeutralBlue else TextSecondary
                                )
                                if (state.openPositions.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .background(BullishGreen, CircleShape)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            "${state.openPositions.size}",
                                            color = TerminalDark,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content
                when (selectedTab) {
                    0 -> RegistrationTabContent(
                        state = state,
                        onSaveCredentials = onSaveCredentials,
                        onAuthCodeSubmit = {
                            onAuthCodeSubmit(it)
                            selectedTab = 1
                        },
                        onConnectDemo = {
                            onConnectDemo()
                            selectedTab = 1
                        }
                    )
                    1 -> TradingTerminalTabContent(
                        state = state,
                        onSelectAccount = onSelectAccount,
                        onConnectDemo = onConnectDemo,
                        onDisconnect = onDisconnect,
                        onClosePosition = onClosePosition,
                        onCloseAllPositions = onCloseAllPositions,
                        onOpenRegistration = { selectedTab = 0 }
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistrationTabContent(
    state: CTraderAuthState,
    onSaveCredentials: (String, String, String) -> Unit,
    onAuthCodeSubmit: (String) -> Unit,
    onConnectDemo: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    var clientIdInput by remember(state.clientId) { mutableStateOf(state.clientId) }
    var clientSecretInput by remember(state.clientSecret) { mutableStateOf(state.clientSecret) }
    var redirectUriInput by remember(state.redirectUri) { mutableStateOf(state.redirectUri) }
    var manualAuthCode by remember { mutableStateOf("") }
    var showCredentialsEditor by remember { mutableStateOf(!state.hasClientCredentials) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        // Step 1: Open API Portal Instructions Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                color = Color(0xFF8B5CF6).copy(alpha = 0.08f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Portal",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Step 1: Register at openapi.ctrader.com/apps",
                            color = Color(0xFFA78BFA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Register your free app on the Spotware Open API portal to trade live or demo accounts. Copy these parameters:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CopyableField("App Name", CTraderAppRegistrationDetails.APP_NAME, clipboard, context)
                    Spacer(modifier = Modifier.height(6.dp))
                    CopyableField("Redirect URI", redirectUriInput, clipboard, context)
                    Spacer(modifier = Modifier.height(6.dp))
                    CopyableField("Required Scopes", "trading accounts", clipboard, context)

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CTraderAppRegistrationDetails.PORTAL_URL))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("open_ctrader_portal_button")
                    ) {
                        Text("Open cTrader Developer Portal", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Step 2: Credentials Configuration
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp)),
                color = TerminalSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step 2: Spotware Client Credentials",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (state.hasClientCredentials) BullishGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (state.hasClientCredentials) "READY" else "NEEDED",
                                color = if (state.hasClientCredentials) BullishGreen else WarningAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (showCredentialsEditor) {
                        Text("Enter the Client ID & Secret from your Spotware developer portal:", color = TextMuted, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = clientIdInput,
                            onValueChange = { clientIdInput = it },
                            label = { Text("Client ID", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("ctrader_client_id_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeutralBlue,
                                unfocusedBorderColor = TerminalBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = clientSecretInput,
                            onValueChange = { clientSecretInput = it },
                            label = { Text("Client Secret", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("ctrader_client_secret_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeutralBlue,
                                unfocusedBorderColor = TerminalBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                onSaveCredentials(clientIdInput, clientSecretInput, redirectUriInput)
                                showCredentialsEditor = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeutralBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("save_ctrader_creds_button")
                        ) {
                            Text("Save Credentials", color = TerminalDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Client ID: ${state.clientId.take(8)}••••••••",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Edit",
                                color = NeutralBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showCredentialsEditor = true }
                            )
                        }
                    }

                    if (state.hasClientCredentials) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val url = CTraderAppRegistrationDetails.buildOAuthUrl(
                                    clientId = clientIdInput,
                                    redirectUri = redirectUriInput
                                )
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("oauth_authorize_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Authorize with Spotware cTrader ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Step 3: Authorization Code Manual Entry (Alternative / Fallback)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp)),
                color = TerminalSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Step 3: Complete Authorization",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "After authorizing in the browser, paste the authorization code below if not automatically redirected:",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualAuthCode,
                            onValueChange = { manualAuthCode = it },
                            placeholder = { Text("Paste code parameter here", fontSize = 11.sp, color = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("manual_auth_code_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeutralBlue,
                                unfocusedBorderColor = TerminalBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                if (manualAuthCode.isNotBlank()) {
                                    onAuthCodeSubmit(manualAuthCode.trim())
                                }
                            },
                            enabled = manualAuthCode.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("submit_auth_code_button")
                        ) {
                            Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Demo / Sandbox Instant Connect
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                color = TerminalSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Instant Testing: cTrader Demo Sandbox",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Test SCT trade routing immediately on a simulated Spotware cTrader Demo account with $50,000 USD balance and 1:500 leverage.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onConnectDemo,
                        modifier = Modifier.fillMaxWidth().testTag("connect_demo_sandbox_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TerminalDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Connect Demo Sandbox Account ($50k)", color = TerminalDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TradingTerminalTabContent(
    state: CTraderAuthState,
    onSelectAccount: (CTraderAccount) -> Unit,
    onConnectDemo: () -> Unit,
    onDisconnect: () -> Unit,
    onClosePosition: (Long) -> Unit,
    onCloseAllPositions: () -> Unit,
    onOpenRegistration: () -> Unit
) {
    val account = state.activeAccount

    if (account == null) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "No cTrader Account Connected",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Connect via cTrader Open API or start the demo sandbox to trade SCT signals.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onConnectDemo,
                colors = ButtonDefaults.buttonColors(containerColor = NeutralBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Start cTrader Demo Account ($50k)", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onOpenRegistration,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("View Open API Registration Details", color = TextSecondary)
            }
        }
        return
    }

    val currencyFormatter = remember { DecimalFormat("#,##0.00") }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        // Account Status Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, TerminalBorder, RoundedCornerShape(14.dp)),
                color = TerminalSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (account.isLive) "LIVE" else "DEMO",
                                    color = if (account.isLive) BullishGreen else GoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = account.accountNumber,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = account.brokerName,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = onDisconnect,
                            colors = ButtonDefaults.buttonColors(containerColor = BearishRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Disconnect", color = BearishRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Available accounts selector if user has more than 1
                    if (state.availableAccounts.size > 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Select Account:", color = TextMuted, fontSize = 10.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.availableAccounts.forEach { acc ->
                                val isCur = acc.accountId == account.accountId
                                Box(
                                    modifier = Modifier
                                        .background(if (isCur) NeutralBlue else TerminalSurfaceCard, RoundedCornerShape(6.dp))
                                        .border(1.dp, if (isCur) NeutralBlue else TerminalBorder, RoundedCornerShape(6.dp))
                                        .clickable { onSelectAccount(acc) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = acc.accountNumber,
                                        color = if (isCur) TerminalDark else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Balance", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = "$${currencyFormatter.format(account.balance)}",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Equity", color = TextMuted, fontSize = 10.sp)
                            val eqColor = if (account.equity >= account.balance) BullishGreen else BearishRed
                            Text(
                                text = "$${currencyFormatter.format(account.equity)}",
                                color = eqColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Free Margin", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = "$${currencyFormatter.format(account.freeMargin)}",
                                color = BullishGreen,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Margin Used: $${currencyFormatter.format(account.margin)}", color = TextSecondary, fontSize = 10.sp)
                        Text("Leverage: 1:${account.leverage}", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }

        // Positions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Open Positions (${state.openPositions.size})",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                if (state.openPositions.isNotEmpty()) {
                    Text(
                        text = "Close All",
                        color = BearishRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onCloseAllPositions() }.testTag("close_all_ctrader_positions")
                    )
                }
            }
        }

        // Positions List
        if (state.openPositions.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = TerminalSurfaceElevated
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No open positions on cTrader", color = TextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Open any SCT Signal and tap 'Trade on cTrader' to place orders with automatic Asian range Stop Loss and PDH/PDL targets.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(state.openPositions, key = { it.positionId }) { pos ->
                PositionItemCard(
                    pos = pos,
                    onClose = { onClosePosition(pos.positionId) }
                )
            }
        }
    }
}

@Composable
private fun PositionItemCard(
    pos: CTraderPosition,
    onClose: () -> Unit
) {
    val isWin = pos.netProfit >= 0
    val pnlColor = if (isWin) BullishGreen else BearishRed

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(10.dp)),
        color = TerminalSurfaceElevated
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (pos.isBullish) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = pos.tradeSide,
                            color = if (pos.isBullish) BullishGreen else BearishRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = pos.symbol,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${pos.volumeLots} lots",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "${if (pos.netProfit >= 0) "+$" else "-$"}${String.format("%.2f", abs(pos.netProfit))}",
                    color = pnlColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Entry: ${String.format("%.5f", pos.entryPrice)} → Cur: ${String.format("%.5f", pos.currentPrice)}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Close",
                    color = BearishRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onClose() }
                )
            }
        }
    }
}

@Composable
private fun CopyableField(
    label: String,
    value: String,
    clipboard: ClipboardManager,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurfaceElevated, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextMuted, fontSize = 9.sp)
            Text(value, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
        }
        IconButton(
            onClick = {
                clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeutralBlue, modifier = Modifier.size(14.dp))
        }
    }
}
