package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.ui.components.ApiDiagnosticsDialog
import com.example.ui.components.CTraderOrderDialog
import com.example.ui.components.CTraderTerminalDialog
import com.example.ui.components.DxTradeConnectDialog
import com.example.ui.components.DxTradeOrderDialog
import com.example.ui.components.DxTradePositionsDialog
import com.example.ui.components.NinjaSettingsDialog
import com.example.ui.components.NotificationAlertsDialog
import com.example.ui.components.PairSelectionDialog
import com.example.ui.components.RiskCalculatorDialog
import com.example.ui.components.TimeZoneSwitchDialog
import com.example.ui.screens.EconomicCalendarScreen
import com.example.ui.screens.FiveDaySetupScreen
import com.example.ui.screens.MarketsScreen
import com.example.ui.screens.MoversScreen
import com.example.ui.screens.SignalDetailScreen
import com.example.ui.screens.SignalsScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeutralBlue
import com.example.ui.theme.TerminalDark
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            MyApplicationTheme {
                TradingSignalApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "sctquotes" && uri.host == "oauth") {
                val code = uri.getQueryParameter("code")
                if (!code.isNullOrEmpty()) {
                    viewModel.handleCTraderAuthCode(code)
                }
            }
        }
    }
}

@Composable
fun TradingSignalApp(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val calcBalance by viewModel.calcBalance.collectAsState()
    val calcRiskPercent by viewModel.calcRiskPercent.collectAsState()
    val calcResult by viewModel.calcResult.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()

    // Notification permission launcher for Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setToast("Notifications enabled for SCT signal alerts")
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Show toast message if requested
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Back navigation to Signals home if on a secondary tab
    if (uiState.selectedSignal == null && uiState.selectedTab != 0) {
        BackHandler {
            viewModel.selectTab(0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = TerminalDark,
        bottomBar = {
            if (uiState.selectedSignal == null) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar"),
                    containerColor = TerminalSurface,
                    contentColor = TextPrimary
                ) {
                    val tabs = listOf(
                        Quad(0, "Signals", Icons.Filled.SignalCellularAlt, Icons.Outlined.SignalCellularAlt),
                        Quad(1, "Markets", Icons.Filled.ShowChart, Icons.Outlined.ShowChart),
                        Quad(2, "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                        Quad(3, "Movers", Icons.Filled.FlashOn, Icons.Outlined.FlashOn),
                        Quad(4, "Watchlist", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder)
                    )

                    tabs.forEach { (index, label, filledIcon, outlinedIcon) ->
                        val isSelected = uiState.selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(index) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) filledIcon else outlinedIcon,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TerminalDark,
                                selectedTextColor = NeutralBlue,
                                indicatorColor = NeutralBlue,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_item_$label")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TerminalDark)
        ) {
            val selectedSignal = uiState.selectedSignal
            if (selectedSignal != null) {
                SignalDetailScreen(
                    signal = selectedSignal,
                    viewModel = viewModel
                )
            } else {
                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> SignalsScreen(
                            viewModel = viewModel,
                            onSignalClick = { viewModel.openSignalDetail(it) }
                        )
                        1 -> MarketsScreen(
                            viewModel = viewModel,
                            onSelectInstrument = { viewModel.openSignalDetail(it) }
                        )
                        2 -> EconomicCalendarScreen(
                            viewModel = viewModel,
                            onSelectPair = { pairSymbol ->
                                val sig = viewModel.allSignals.value.find { it.symbol == pairSymbol }
                                if (sig != null) {
                                    viewModel.openSignalDetail(sig)
                                } else {
                                    viewModel.setToast("Selected $pairSymbol")
                                }
                            }
                        )
                        3 -> MoversScreen(
                            viewModel = viewModel,
                            onSelectInstrument = { viewModel.openSignalDetail(it) }
                        )
                        4 -> WatchlistScreen(
                            viewModel = viewModel,
                            onSignalClick = { viewModel.openSignalDetail(it) }
                        )
                        5 -> FiveDaySetupScreen(
                            viewModel = viewModel,
                            onOpenSignalDetail = { viewModel.openSignalDetail(it) }
                        )
                    }
                }
            }
        }

        // Time Zone Switch Modal Dialog
        if (uiState.showTimeZoneDialog) {
            val currentTimeZone by viewModel.selectedTimeZone.collectAsState()
            TimeZoneSwitchDialog(
                currentTimeZone = currentTimeZone,
                onSelectTimeZone = { tz ->
                    viewModel.setTimeZone(tz)
                    viewModel.showTimeZoneDialog(false)
                    viewModel.setToast("Active Time Zone: ${tz.displayName}")
                },
                onDismiss = { viewModel.showTimeZoneDialog(false) }
            )
        }

        // Risk Calculator Modal Dialog
        if (uiState.showRiskCalculator) {
            val signal = uiState.selectedSignal ?: viewModel.filteredSignals.collectAsState().value.firstOrNull()
            if (signal != null) {
                RiskCalculatorDialog(
                    signal = signal,
                    onDismiss = { viewModel.showRiskCalculator(false) }
                )
            }
        }

        // Biquote API Diagnostics Modal Dialog
        if (uiState.showDiagnostics) {
            ApiDiagnosticsDialog(
                diagnostics = diagnostics,
                onTestPing = { viewModel.refreshData() },
                onDismiss = { viewModel.showDiagnostics(false) }
            )
        }

        // Pair Selection Onboarding & Management Modal Dialog
        if (uiState.showPairSelectionDialog) {
            val selectedPairs by viewModel.selectedPairs.collectAsState()
            val allSignals by viewModel.allSignals.collectAsState()
            PairSelectionDialog(
                allInstruments = viewModel.repository.supportedInstruments,
                selectedPairs = selectedPairs,
                currentSignals = allSignals,
                onSaveSelection = { pairs ->
                    viewModel.updateSelectedPairs(pairs)
                },
                onDismiss = {
                    viewModel.showPairSelectionDialog(false)
                },
                isOnboarding = uiState.isFirstLaunch
            )
        }

        // Ninja USDJPY Institutional Settings Modal Dialog
        if (uiState.showNinjaSettingsDialog) {
            val sctSettings by viewModel.sctSettings.collectAsState()
            NinjaSettingsDialog(
                initialSettings = sctSettings.ninjaSettings,
                onSaveSettings = { updatedNinja ->
                    viewModel.updateNinjaSettings(updatedNinja)
                    viewModel.openNinjaSettings(false)
                },
                onDismiss = { viewModel.openNinjaSettings(false) }
            )
        }

        // DXtrade REST API Account Connection Modal Dialog
        if (uiState.showDxTradeConnectDialog) {
            val connState by viewModel.dxConnectionState.collectAsState()
            val account by viewModel.dxAccount.collectAsState()
            val creds by viewModel.dxCredentials.collectAsState()
            DxTradeConnectDialog(
                connectionState = connState,
                account = account,
                savedCredentials = creds,
                onConnect = { c -> viewModel.connectDxTrade(c) },
                onConnectDemo = { viewModel.connectDxTradeDemo() },
                onDisconnect = { viewModel.disconnectDxTrade() },
                onDismiss = { viewModel.openDxTradeConnect(false) }
            )
        }

        // DXtrade Instant Order Execution Modal Dialog (Trade from SCT Quotes)
        if (uiState.showDxTradeOrderDialog) {
            val targetSignal = uiState.tradeTargetSignal ?: uiState.selectedSignal ?: viewModel.filteredSignals.collectAsState().value.firstOrNull()
            if (targetSignal != null) {
                val account by viewModel.dxAccount.collectAsState()
                DxTradeOrderDialog(
                    signal = targetSignal,
                    account = account,
                    onExecuteOrder = { req ->
                        viewModel.executeDxTradeOrder(req) { success, _ ->
                            if (success) {
                                viewModel.closeDxTradeOrder()
                            }
                        }
                    },
                    onOpenConnectDialog = {
                        viewModel.closeDxTradeOrder()
                        viewModel.openDxTradeConnect(true)
                    },
                    onDismiss = { viewModel.closeDxTradeOrder() }
                )
            }
        }

        // DXtrade Open Positions & P&L Manager Modal Dialog
        if (uiState.showDxTradePositionsDialog) {
            val positions by viewModel.dxPositions.collectAsState()
            val account by viewModel.dxAccount.collectAsState()
            DxTradePositionsDialog(
                positions = positions,
                account = account,
                onClosePosition = { posId -> viewModel.closeDxTradePosition(posId) },
                onDismiss = { viewModel.openDxTradePositions(false) }
            )
        }

        // Spotware cTrader Open API Terminal Modal Dialog
        if (uiState.showCTraderTerminalDialog) {
            val ctraderAuth by viewModel.ctraderAuthState.collectAsState()
            CTraderTerminalDialog(
                state = ctraderAuth,
                onSaveCredentials = { id, secret, uri ->
                    viewModel.saveCTraderCredentials(id, secret, uri)
                },
                onAuthCodeSubmit = { code ->
                    viewModel.handleCTraderAuthCode(code)
                },
                onSelectAccount = { acc ->
                    viewModel.selectCTraderAccount(acc)
                },
                onConnectDemo = { viewModel.connectCTraderDemo() },
                onDisconnect = { viewModel.disconnectCTrader() },
                onClosePosition = { posId -> viewModel.closeCTraderPosition(posId) },
                onCloseAllPositions = { viewModel.closeAllCTraderPositions() },
                onDismiss = { viewModel.openCTraderTerminal(false) }
            )
        }

        // Spotware cTrader Instant Order Execution Modal Dialog
        if (uiState.showCTraderOrderDialog) {
            val targetSignal = uiState.ctraderTargetSignal
                ?: uiState.selectedSignal
                ?: viewModel.filteredSignals.collectAsState().value.firstOrNull()
            if (targetSignal != null) {
                val ctraderAuth by viewModel.ctraderAuthState.collectAsState()
                CTraderOrderDialog(
                    signal = targetSignal,
                    account = ctraderAuth.activeAccount,
                    onExecuteOrder = { req ->
                        viewModel.executeCTraderOrder(req)
                    },
                    onOpenConnectDialog = {
                        viewModel.closeCTraderOrder()
                        viewModel.openCTraderTerminal(true)
                    },
                    onDismiss = { viewModel.closeCTraderOrder() }
                )
            }
        }

        // Signal Notification Alerts Modal Dialog
        if (uiState.showNotificationAlertsDialog) {
            val alerts by viewModel.triggerAlerts.collectAsState()
            val alertsEnabled by viewModel.alertsEnabled.collectAsState()
            val ninjaAlertsEnabled by viewModel.ninjaAlertsEnabled.collectAsState()
            val sctSettings by viewModel.sctSettings.collectAsState()

            NotificationAlertsDialog(
                alerts = alerts,
                alertsEnabled = alertsEnabled,
                ninjaAlertsEnabled = ninjaAlertsEnabled,
                isNinjaEngineActive = sctSettings.ninjaSettings.enabled,
                onToggleAlerts = { viewModel.toggleAlerts(it) },
                onToggleNinjaAlerts = { viewModel.toggleNinjaAlerts(it) },
                onTestNotification = { viewModel.simulateTestTriggerAlert() },
                onClearAlerts = { viewModel.clearTriggerAlerts() },
                onSelectAlertSymbol = { sym ->
                    val sig = viewModel.allSignals.value.find { it.symbol == sym }
                    if (sig != null) {
                        viewModel.openSignalDetail(sig)
                    }
                },
                onDismiss = { viewModel.showNotificationAlertsDialog(false) }
            )
        }

        // Fullscreen Signals Slideshow Modal Dialog
        if (uiState.showFullSlideshowDialog) {
            val selectedPairs by viewModel.selectedPairs.collectAsState()
            val allSignals by viewModel.allSignals.collectAsState()
            PairSelectionDialog(
                allInstruments = viewModel.repository.supportedInstruments,
                selectedPairs = selectedPairs,
                currentSignals = allSignals,
                onSaveSelection = { pairs ->
                    viewModel.updateSelectedPairs(pairs)
                },
                onDismiss = {
                    viewModel.showFullSlideshowDialog(false)
                },
                isOnboarding = false,
                initialTab = 1
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
