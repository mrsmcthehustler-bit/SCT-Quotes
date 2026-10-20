package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.delay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import com.example.data.model.DxTradeConnectionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AssetClass
import com.example.data.model.EnumSctMode
import com.example.data.model.SupportedTimeZones
import com.example.data.model.TradingSignal
import com.example.ui.components.AssetBadge
import com.example.ui.components.ConnectionStatusBadge
import com.example.ui.components.SignalCard
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeutralBlue
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalDark
import com.example.ui.theme.TerminalSurfaceCard
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SignalsScreen(
    viewModel: MainViewModel,
    onSignalClick: (TradingSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val signals by viewModel.filteredSignals.collectAsState()
    val allSignals by viewModel.allSignals.collectAsState()
    val ticks by viewModel.allTicks.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()
    val sctSettings by viewModel.sctSettings.collectAsState()
    val timeZone by viewModel.selectedTimeZone.collectAsState()
    val dxConnectionState by viewModel.dxConnectionState.collectAsState()
    val selectedPairs by viewModel.selectedPairs.collectAsState()
    val unreadAlertsCount by viewModel.unreadAlertsCount.collectAsState()
    val isNinjaEnabled = sctSettings.ninjaSettings.enabled

    var showSearch by remember { mutableStateOf(false) }

    // Signals of the pairs selected upon start for the Slideshow feature
    val slideshowSignals = remember(allSignals, selectedPairs, isNinjaEnabled) {
        val subscribed = allSignals.filter { selectedPairs.contains(it.symbol) }
        if (subscribed.isNotEmpty()) subscribed else allSignals
    }

    var activeSlideIndex by remember { mutableIntStateOf(0) }
    var isSlideshowAutoPlaying by remember { mutableStateOf(true) }

    // Auto-advance slideshow on the Signals tab
    LaunchedEffect(isSlideshowAutoPlaying, slideshowSignals.size, activeSlideIndex) {
        if (isSlideshowAutoPlaying && slideshowSignals.size > 1) {
            delay(4000L)
            activeSlideIndex = (activeSlideIndex + 1) % slideshowSignals.size
        }
    }

    // Featured Spotlight Signal: Active slide from slideshow of selected pairs
    val featuredSignal = remember(slideshowSignals, activeSlideIndex, allSignals, sctSettings.operationalMode, isNinjaEnabled) {
        if (slideshowSignals.isNotEmpty()) {
            slideshowSignals[activeSlideIndex.coerceIn(0, slideshowSignals.size - 1)]
        } else if (isNinjaEnabled) {
            allSignals.find { it.symbol == "USDJPY" } ?: allSignals.firstOrNull()
        } else if (sctSettings.operationalMode == EnumSctMode.MODE_EURO) {
            allSignals.find { it.symbol == "EURUSD" } ?: allSignals.firstOrNull()
        } else {
            allSignals.find { it.symbol == "GBPUSD" } ?: allSignals.find { it.symbol == "USDJPY" } ?: allSignals.firstOrNull()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalDark)
            .testTag("signals_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
                    text = "SCT Quotes",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .background(NeutralBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, NeutralBlue, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "v5.40",
                        color = NeutralBlue,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // DXtrade REST API Connection / Orders Pill
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.dp,
                            if (dxConnectionState == DxTradeConnectionState.CONNECTED) BullishGreen else NeutralBlue.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            if (dxConnectionState == DxTradeConnectionState.CONNECTED) {
                                viewModel.openDxTradePositions(true)
                            } else {
                                viewModel.openDxTradeConnect(true)
                            }
                        }
                        .testTag("dxtrade_top_pill"),
                    color = TerminalSurfaceElevated,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "DXtrade",
                            tint = if (dxConnectionState == DxTradeConnectionState.CONNECTED) BullishGreen else NeutralBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (dxConnectionState == DxTradeConnectionState.CONNECTED) "DXtrade" else "Trade DX",
                            color = if (dxConnectionState == DxTradeConnectionState.CONNECTED) BullishGreen else NeutralBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Spotware cTrader Open API Connection / Accounts Pill
                val ctraderState by viewModel.ctraderAuthState.collectAsState()
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.dp,
                            if (ctraderState.isAuthorized) BullishGreen else Color(0xFF8B5CF6).copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            viewModel.openCTraderTerminal(true)
                        }
                        .testTag("ctrader_top_pill"),
                    color = TerminalSurfaceElevated,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "cTrader",
                            tint = if (ctraderState.isAuthorized) BullishGreen else Color(0xFFA78BFA),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (ctraderState.isAuthorized) "cTrader" else "cTrader API",
                            color = if (ctraderState.isAuthorized) BullishGreen else Color(0xFFA78BFA),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Subscribed Pairs Selector Pill
                val userSubscribedPairs by viewModel.selectedPairs.collectAsState()
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .clickable { viewModel.showPairSelectionDialog(true) }
                        .testTag("subscribed_pairs_top_pill"),
                    color = TerminalSurfaceElevated,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "My Pairs",
                            tint = NeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Pairs (${userSubscribedPairs.size})",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Connection Status Badge
                ConnectionStatusBadge(
                    diagnostics = diagnostics,
                    countdownSeconds = uiState.countdownSeconds,
                    onClick = { viewModel.showDiagnostics(true) }
                )

                // Notification Alerts Bell with Unread Badge
                Box {
                    IconButton(
                        onClick = { viewModel.showNotificationAlertsDialog(true) },
                        modifier = Modifier.size(36.dp).testTag("top_notification_alerts_button")
                    ) {
                        Icon(
                            imageVector = if (unreadAlertsCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Notification Alerts",
                            tint = if (unreadAlertsCount > 0) NeonCyan else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (unreadAlertsCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                                .background(Color(0xFFF43F5E), CircleShape)
                                .border(1.dp, TerminalDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadAlertsCount > 9) "9+" else unreadAlertsCount.toString(),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Fullscreen Signals Slideshow Launch Button
                IconButton(
                    onClick = { viewModel.showFullSlideshowDialog(true) },
                    modifier = Modifier.size(36.dp).testTag("top_slideshow_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Slideshow,
                        contentDescription = "Signals Slideshow",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Search Toggle
                IconButton(
                    onClick = {
                        showSearch = !showSearch
                        if (!showSearch) viewModel.setSearchQuery("")
                    },
                    modifier = Modifier.testTag("toggle_search_button")
                ) {
                    Icon(
                        imageVector = if (showSearch) Icons.Default.Clear else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Quick Refresh
                IconButton(
                    onClick = { viewModel.refreshData() },
                    modifier = Modifier.testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = NeutralBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Search Bar if toggled
        if (showSearch) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search EURUSD, GBPUSD, BTC, Gold…", color = TextMuted, fontSize = 13.sp) },
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
                        .testTag("search_input")
                )
            }
        }

        // Main Scrollable Welcome Feed
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 85.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SCT Institutional Session Tracker (Interactive Time Zone switcher)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TerminalSurfaceElevated)
                        .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.showTimeZoneDialog(true) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("session_tracker_pill"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(BullishGreen, CircleShape)
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "LONDON / FRANKFURT WINDOW",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "• ${timeZone.abbreviation}",
                                    color = NeutralBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Asian: ${SupportedTimeZones.formatAsianRangeWindow(timeZone)} • Tap to switch zone",
                                color = Color(0xFFA78BFA),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = SupportedTimeZones.formatCurrentClock(timeZone),
                            color = Color(0xFFA78BFA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // NINJA USDJPY FOCUS CARD & CONDITIONAL "NINJA SETTINGS" OPTION
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.dp,
                            if (isNinjaEnabled) GoldAccent else TerminalBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .testTag("ninja_mode_card"),
                    color = if (isNinjaEnabled) Color(0xFF1E1708) else TerminalSurfaceCard,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
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
                                        .size(34.dp)
                                        .background(
                                            if (isNinjaEnabled) GoldAccent.copy(alpha = 0.25f) else TerminalSurfaceElevated,
                                            CircleShape
                                        )
                                        .border(1.dp, if (isNinjaEnabled) GoldAccent else TerminalBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🥷", fontSize = 16.sp)
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Ninja",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isNinjaEnabled) GoldAccent.copy(alpha = 0.25f) else TerminalSurfaceElevated,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .border(
                                                    0.5.dp,
                                                    if (isNinjaEnabled) GoldAccent else TerminalBorder,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "USDJPY FOCUS",
                                                color = if (isNinjaEnabled) GoldAccent else TextMuted,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isNinjaEnabled) "Active • 100% Focus on USDJPY (SCT Fixed V2.80)" else "Switch Ninja on to focus exclusively on USDJPY",
                                        color = if (isNinjaEnabled) GoldAccent else TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Switch to activate / deactivate Ninja Mode
                            Switch(
                                checked = isNinjaEnabled,
                                onCheckedChange = { checked ->
                                    viewModel.toggleNinja(checked)
                                },
                                modifier = Modifier.testTag("ninja_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TerminalDark,
                                    checkedTrackColor = GoldAccent,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = TerminalSurfaceElevated
                                )
                            )
                        }

                        // Ninja settings option: WILL ONLY APPEAR IF USER SWITCHES NINJA ON!
                        if (isNinjaEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = GoldAccent.copy(alpha = 0.35f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Ninja Settings & Parameters",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Buffer: ${sctSettings.ninjaSettings.slBufferPoints} pts • Breakout: ${if (sctSettings.ninjaSettings.tradeZoneBreakouts) "Direct" else "OB"} • TP Hunter: ${if (sctSettings.ninjaSettings.enableTPHunter) "ON" else "OFF"}",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Option called "Ninja settings"
                                Button(
                                    onClick = { viewModel.openNinjaSettings(true) },
                                    modifier = Modifier.testTag("ninja_settings_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Ninja settings",
                                        tint = TerminalDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Ninja settings",
                                        color = TerminalDark,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. HERO HEADLINE BANNER (Alive, Robust & Institutional)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, Color(0xFF1E3A2F), RoundedCornerShape(20.dp))
                        .testTag("hero_headline_banner"),
                    color = TerminalSurfaceCard,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Background Hero Graphic
                        Image(
                            painter = painterResource(id = R.drawable.img_sct_hero_banner),
                            contentDescription = "SCT Hero Art",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                            contentScale = ContentScale.Crop,
                            alpha = 0.35f
                        )

                        // Rich Gradient Mask for contrast
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xB3090D16),
                                            Color(0xF5090D16)
                                        )
                                    )
                                )
                        )

                        // Foreground Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            // Institutional Badge Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF14532D).copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                        .border(1.dp, BullishGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FlashOn,
                                            contentDescription = null,
                                            tint = BullishGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "SCT THEORY INSTITUTIONAL",
                                            color = BullishGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "patience • precision • diligence",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // HERO HEADLINE
                            Text(
                                text = "Trade the Session.\nExecute with Precision.",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                lineHeight = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.2.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Real-time Biquote liquidity integrated with EMA 9/21 ribbons, Asian session ranges, and Fibonacci retracements.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Live Performance Pulse Ticker Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(TerminalSurfaceElevated.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                                    .border(1.dp, TerminalBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Strategy Mode", color = TextMuted, fontSize = 9.sp)
                                    Text(
                                        text = sctSettings.operationalMode.name,
                                        color = NeutralBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Win Rate", color = TextMuted, fontSize = 9.sp)
                                    Text(
                                        text = "70.0% (14W / 6L)",
                                        color = BullishGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Net Pips", color = TextMuted, fontSize = 9.sp)
                                    Text(
                                        text = "+116.0 Pips",
                                        color = GoldAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. QUICK ACTION STATION
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionCard(
                            title = "Markets Overview",
                            subtitle = "SCT Universe",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconTint = NeutralBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectTab(1) }
                        )
                        QuickActionCard(
                            title = "Eco Calendar",
                            subtitle = "Macro Catalysts",
                            icon = Icons.Default.CalendarMonth,
                            iconTint = Color(0xFFF43F5E),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectTab(2) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionCard(
                            title = if (sctSettings.operationalMode == EnumSctMode.MODE_EURO) "Euro Mode" else "USD Mode",
                            subtitle = "Toggle Strategy",
                            icon = Icons.Default.SwapHoriz,
                            iconTint = BullishGreen,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val nextMode = if (sctSettings.operationalMode == EnumSctMode.MODE_EURO) EnumSctMode.MODE_USD else EnumSctMode.MODE_EURO
                                viewModel.updateSctSettings(sctSettings.copy(operationalMode = nextMode))
                                viewModel.setToast("Switched to ${nextMode.name}")
                            }
                        )
                        QuickActionCard(
                            title = "Risk Calc",
                            subtitle = "Lot & Risk %",
                            icon = Icons.Default.Calculate,
                            iconTint = GoldAccent,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.showRiskCalculator(true) }
                        )
                    }
                }
            }

            // 3. FEATURED SPOTLIGHT CARD (With Live Horizontal Fill Bar!)
            if (featuredSignal != null) {
                item {
                    val tick = ticks[featuredSignal.symbol]
                    val livePrice = tick?.currentPrice ?: featuredSignal.currentPrice
                    val isBull = featuredSignal.signalType.isBullish

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, NeutralBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .testTag("featured_spotlight_card"),
                        color = TerminalSurfaceCard,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Slideshow Header Controls Row
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
                                            .size(8.dp)
                                            .background(if (isBull) BullishGreen else BearishRed, CircleShape)
                                    )
                                    Text(
                                        text = "FEATURED SCT SLIDESHOW",
                                        color = NeutralBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                // Carousel Controls: Prev, Indicator, Autoplay, Next, Expand
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (slideshowSignals.size > 1) {
                                        // Prev button
                                        IconButton(
                                            onClick = {
                                                activeSlideIndex = if (activeSlideIndex > 0) activeSlideIndex - 1 else slideshowSignals.size - 1
                                            },
                                            modifier = Modifier.size(26.dp).testTag("spotlight_slideshow_prev")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Prev",
                                                tint = NeonCyan,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        // Slide counter
                                        Text(
                                            text = "${activeSlideIndex + 1}/${slideshowSignals.size}",
                                            color = TextPrimary,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )

                                        // Autoplay toggle pill
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(0.5.dp, if (isSlideshowAutoPlaying) BullishGreen else TextMuted, RoundedCornerShape(8.dp))
                                                .clickable { isSlideshowAutoPlaying = !isSlideshowAutoPlaying }
                                                .testTag("spotlight_autoplay_toggle"),
                                            color = TerminalDark
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSlideshowAutoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = if (isSlideshowAutoPlaying) BullishGreen else TextMuted,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Text(
                                                    text = if (isSlideshowAutoPlaying) "Auto" else "Pause",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSlideshowAutoPlaying) BullishGreen else TextMuted
                                                )
                                            }
                                        }

                                        // Next button
                                        IconButton(
                                            onClick = {
                                                activeSlideIndex = (activeSlideIndex + 1) % slideshowSignals.size
                                            },
                                            modifier = Modifier.size(26.dp).testTag("spotlight_slideshow_next")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Next",
                                                tint = NeonCyan,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }

                                    // Expand to Fullscreen Slideshow button
                                    IconButton(
                                        onClick = { viewModel.showFullSlideshowDialog(true) },
                                        modifier = Modifier.size(26.dp).testTag("spotlight_fullscreen_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInFull,
                                            contentDescription = "Fullscreen",
                                            tint = TextMuted,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Selected Pairs Quick-Jump Ribbon
                            if (slideshowSignals.size > 1) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    items(slideshowSignals.indices.toList()) { idx ->
                                        val sig = slideshowSignals[idx]
                                        val isActive = idx == activeSlideIndex
                                        val isSigBull = sig.signalType.isBullish
                                        val isNinjaPair = isNinjaEnabled && sig.symbol == "USDJPY"

                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(
                                                    1.dp,
                                                    if (isActive) (if (isNinjaPair) Color(0xFFF59E0B) else NeonCyan) else TerminalBorder,
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable { activeSlideIndex = idx }
                                                .testTag("slideshow_chip_${sig.symbol}"),
                                            color = if (isActive) (if (isNinjaPair) Color(0xFFF59E0B).copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.15f)) else TerminalSurfaceElevated
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(if (isSigBull) BullishGreen else BearishRed, CircleShape)
                                                )
                                                Text(
                                                    text = sig.symbol,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                                                    color = if (isActive) (if (isNinjaPair) Color(0xFFF59E0B) else NeonCyan) else TextSecondary,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                if (isNinjaPair) {
                                                    Text(
                                                        text = "🥷",
                                                        fontSize = 8.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Symbol & Strategy info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = featuredSignal.symbol,
                                            color = TextPrimary,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        AssetBadge(assetClass = featuredSignal.assetClass)
                                        if (isNinjaEnabled && featuredSignal.symbol == "USDJPY") {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFF59E0B))
                                            ) {
                                                Text(
                                                    text = "NINJA M5",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFFF59E0B),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = featuredSignal.sctStrategyType,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isBull) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isBull) BullishGreen else BearishRed,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = featuredSignal.signalType.label,
                                        color = if (isBull) BullishGreen else BearishRed,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Featured SCT Setup Level Strip
                            featuredSignal.sctLevels?.let { sct ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(TerminalSurfaceElevated, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Asian Range Box", color = TextMuted, fontSize = 9.sp)
                                        Text("${String.format("%.4f", sct.asianLow)} - ${String.format("%.4f", sct.asianHigh)}", color = Color(0xFFA78BFA), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Retest Level", color = TextMuted, fontSize = 9.sp)
                                        Text(String.format("%.4f", sct.retestLevel ?: sct.rangeHigh), color = NeutralBlue, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Liquidity Target", color = TextMuted, fontSize = 9.sp)
                                        Text(if (isBull) "PDH: ${String.format("%.4f", sct.pdh)}" else "PDL: ${String.format("%.4f", sct.pdl)}", color = Color(0xFFF43F5E), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Buttons: Trade on DXtrade & Open Analysis
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.openDxTradeOrder(featuredSignal) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("spotlight_trade_dxtrade_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isBull) BullishGreen else BearishRed
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = "Trade DXtrade",
                                        tint = TerminalDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBull) "BUY (DX)" else "SELL (DX)",
                                        color = TerminalDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Button(
                                    onClick = { viewModel.openCTraderOrder(featuredSignal) },
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .testTag("spotlight_trade_ctrader_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF8B5CF6)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                        contentDescription = "Trade cTrader",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "cTrader",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { onSignalClick(featuredSignal) },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("spotlight_open_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "SCT Levels",
                                            color = NeutralBlue,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = NeutralBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. FEED HEADER & FILTERS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monitored Instruments (${signals.size})",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Tap any card for deep breakdown",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // SCT Strategy Archetype Filter Pills
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(com.example.data.model.SctStrategyArchetype.values()) { arch ->
                        val isSelected = uiState.selectedSctArchetype == arch
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
                                .clickable { viewModel.setSctArchetypeFilter(arch) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("filter_sct_${arch.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = arch.shortLabel,
                                color = if (isSelected) Color(0xFF090D16) else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Direction Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "All Signals",
                        "BUY" to "Bullish Long",
                        "SELL" to "Bearish Short",
                        "STRONG" to "High Conviction"
                    ).forEach { (key, label) ->
                        val isSelected = uiState.selectedDirectionFilter == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) TerminalSurfaceElevated else TerminalDark,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NeutralBlue else TerminalBorder.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setDirectionFilter(key) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) NeutralBlue else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Signal Cards
            if (signals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SignalCellularAlt,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No signals match the filter",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try changing asset filter or clearing your search.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(signals, key = { it.id }) { signal ->
                    SignalCard(
                        signal = signal,
                        onClick = { onSignalClick(signal) },
                        onBookmarkToggle = { viewModel.toggleBookmark(signal.symbol) },
                        onTradeClick = { viewModel.openDxTradeOrder(signal) },
                        onCTraderClick = { viewModel.openCTraderOrder(signal) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("quick_action_${title}"),
        color = TerminalSurfaceElevated,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
