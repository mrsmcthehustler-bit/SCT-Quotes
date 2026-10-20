package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AssetClass
import com.example.data.model.BiquoteTick
import com.example.data.model.EconomicEvent
import com.example.data.model.MarketMover
import com.example.data.model.MarketSummary
import com.example.data.model.RiskCalcResult
import com.example.data.model.SignalType
import com.example.data.model.SupportedTimeZones
import com.example.data.model.TimeZoneSetting
import com.example.data.model.TradingSignal
import com.example.data.repository.ApiDiagnostics
import com.example.data.repository.BiquoteRepository
import com.example.data.repository.EconomicCalendarRepository
import com.example.data.model.PairFiveDayPerformance
import com.example.data.model.SctTriggerAlert
import com.example.data.model.TriggerType
import com.example.data.repository.SctPerformanceEngine
import com.example.util.SctNotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val selectedTab: Int = 0, // 0: Signals, 1: Markets, 2: Calendar, 3: Movers, 4: Watchlist
    val selectedAssetClass: AssetClass = AssetClass.ALL,
    val selectedSctArchetype: com.example.data.model.SctStrategyArchetype = com.example.data.model.SctStrategyArchetype.ALL,
    val selectedDirectionFilter: String = "ALL", // "ALL", "BUY", "SELL", "STRONG"
    val searchQuery: String = "",
    val selectedSignal: TradingSignal? = null,
    val selectedDetailTimeframe: String = "H1",
    val isRefreshing: Boolean = false,
    val autoRefreshEnabled: Boolean = true,
    val countdownSeconds: Int = 15,
    val showRiskCalculator: Boolean = false,
    val showDiagnostics: Boolean = false,
    val showTimeZoneDialog: Boolean = false,
    val showPairSelectionDialog: Boolean = false,
    val isFirstLaunch: Boolean = false,
    val filterOnlySubscribedPairs: Boolean = true,
    val showNinjaSettingsDialog: Boolean = false,
    val showDxTradeConnectDialog: Boolean = false,
    val showDxTradeOrderDialog: Boolean = false,
    val showDxTradePositionsDialog: Boolean = false,
    val tradeTargetSignal: TradingSignal? = null,
    val showCTraderTerminalDialog: Boolean = false,
    val showCTraderOrderDialog: Boolean = false,
    val ctraderTargetSignal: TradingSignal? = null,
    val selectedTimeZone: TimeZoneSetting = SupportedTimeZones.UTC,
    val toastMessage: String? = null,
    val showNotificationAlertsDialog: Boolean = false,
    val showFullSlideshowDialog: Boolean = false,
    val alertsEnabled: Boolean = true,
    val ninjaAlertsEnabled: Boolean = true,
    val slideshowAutoPlay: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = BiquoteRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        UiState(
            showPairSelectionDialog = !repository.isOnboardingCompleted(),
            isFirstLaunch = !repository.isOnboardingCompleted()
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val allSignals: StateFlow<List<TradingSignal>> = repository.signals
    val allTicks: StateFlow<Map<String, BiquoteTick>> = repository.ticks
    val gainers: StateFlow<List<MarketMover>> = repository.gainers
    val losers: StateFlow<List<MarketMover>> = repository.losers
    val mostActive: StateFlow<List<MarketMover>> = repository.mostActive
    val marketSummary: StateFlow<MarketSummary> = repository.marketSummary
    val diagnostics: StateFlow<ApiDiagnostics> = repository.apiDiagnostics
    val sctSettings: StateFlow<com.example.data.model.SctSettings> = repository.sctSettings

    private val _selectedPairs = MutableStateFlow(repository.loadSelectedPairs())
    val selectedPairs: StateFlow<Set<String>> = _selectedPairs.asStateFlow()

    private val _alertsEnabled = MutableStateFlow(repository.loadAlertsEnabled())
    val alertsEnabled: StateFlow<Boolean> = _alertsEnabled.asStateFlow()

    private val _ninjaAlertsEnabled = MutableStateFlow(repository.loadNinjaAlertsEnabled())
    val ninjaAlertsEnabled: StateFlow<Boolean> = _ninjaAlertsEnabled.asStateFlow()

    private val _triggerAlerts = MutableStateFlow<List<SctTriggerAlert>>(emptyList())
    val triggerAlerts: StateFlow<List<SctTriggerAlert>> = _triggerAlerts.asStateFlow()

    private val _unreadAlertsCount = MutableStateFlow(0)
    val unreadAlertsCount: StateFlow<Int> = _unreadAlertsCount.asStateFlow()

    private val alertedTriggerKeys = mutableSetOf<String>()

    private val _selectedTimeZone = MutableStateFlow(repository.loadSavedTimeZone())
    val selectedTimeZone: StateFlow<TimeZoneSetting> = _selectedTimeZone.asStateFlow()

    val calendarRepository = EconomicCalendarRepository(application.applicationContext)
    val economicEvents: StateFlow<List<EconomicEvent>> = calendarRepository.events
    val calendarStatus: StateFlow<com.example.data.model.ForexFactoryCalendarStatus> = calendarRepository.status

    val dxTradeRepository = com.example.data.repository.DxTradeRepository(application.applicationContext)
    val dxConnectionState: StateFlow<com.example.data.model.DxTradeConnectionState> = dxTradeRepository.connectionState
    val dxAccount: StateFlow<com.example.data.model.DxTradeAccount?> = dxTradeRepository.activeAccount
    val dxCredentials: StateFlow<com.example.data.model.DxTradeCredentials> = dxTradeRepository.credentials
    val dxPositions: StateFlow<List<com.example.data.model.DxTradePosition>> = dxTradeRepository.openPositions

    val ctraderRepository = com.example.data.repository.CTraderRepository(application.applicationContext)
    val ctraderAuthState: StateFlow<com.example.data.model.CTraderAuthState> = ctraderRepository.authState

    private val _selectedPerformanceSymbol = MutableStateFlow("EURUSD")
    val selectedPerformanceSymbol: StateFlow<String> = _selectedPerformanceSymbol.asStateFlow()

    val fiveDayPerformance: StateFlow<PairFiveDayPerformance> = combine(
        _selectedPerformanceSymbol,
        allSignals,
        allTicks
    ) { symbol, signals, ticks ->
        val sig = signals.find { it.symbol == symbol }
        val tick = ticks[symbol]
        val name = sig?.name ?: symbol
        val assetClass = sig?.assetClass ?: AssetClass.FOREX
        val price = tick?.currentPrice ?: sig?.currentPrice ?: 1.0
        SctPerformanceEngine.computeFiveDayPerformance(symbol, name, assetClass, price)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SctPerformanceEngine.computeFiveDayPerformance("EURUSD", "Euro / US Dollar", AssetClass.FOREX, 1.0850)
    )

    val nextHighImpactEvent: StateFlow<EconomicEvent?> = combine(
        economicEvents,
        _selectedTimeZone
    ) { events, _ ->
        EconomicCalendarRepository.getNextHighImpactEvent(events)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filtered signals for the Signals Tab
    val filteredSignals: StateFlow<List<TradingSignal>> = combine(
        allSignals,
        _uiState,
        _selectedPairs
    ) { signals, state, subscribedPairs ->
        signals.filter { signal ->
            val matchesSubscribed = !state.filterOnlySubscribedPairs || subscribedPairs.isEmpty() || signal.symbol in subscribedPairs
            val matchesSct = state.selectedSctArchetype == com.example.data.model.SctStrategyArchetype.ALL || signal.sctArchetype == state.selectedSctArchetype
            val matchesAsset = state.selectedAssetClass == AssetClass.ALL || signal.assetClass == state.selectedAssetClass
            val matchesDirection = when (state.selectedDirectionFilter) {
                "BUY" -> signal.signalType.isBullish
                "SELL" -> signal.signalType.isBearish
                "STRONG" -> signal.signalType == SignalType.STRONG_BUY || signal.signalType == SignalType.STRONG_SELL
                else -> true
            }
            val matchesSearch = state.searchQuery.isEmpty() ||
                    signal.symbol.contains(state.searchQuery, ignoreCase = true) ||
                    signal.name.contains(state.searchQuery, ignoreCase = true)

            matchesSubscribed && matchesSct && matchesAsset && matchesDirection && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Watchlist signals
    val watchlistSignals: StateFlow<List<TradingSignal>> = combine(
        allSignals,
        _uiState
    ) { signals, state ->
        signals.filter { it.isBookmarked }.filter {
            state.searchQuery.isEmpty() ||
                    it.symbol.contains(state.searchQuery, ignoreCase = true) ||
                    it.name.contains(state.searchQuery, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Risk calculator state
    private val _calcBalance = MutableStateFlow("10000")
    val calcBalance: StateFlow<String> = _calcBalance.asStateFlow()

    private val _calcRiskPercent = MutableStateFlow("1.5")
    val calcRiskPercent: StateFlow<String> = _calcRiskPercent.asStateFlow()

    private val _calcResult = MutableStateFlow<RiskCalcResult?>(null)
    val calcResult: StateFlow<RiskCalcResult?> = _calcResult.asStateFlow()

    private var autoRefreshJob: Job? = null

    init {
        SctNotificationHelper.initChannel(application)
        _uiState.value = _uiState.value.copy(
            alertsEnabled = _alertsEnabled.value,
            ninjaAlertsEnabled = _ninjaAlertsEnabled.value
        )
        refreshData()
        startAutoRefreshLoop()
        viewModelScope.launch {
            allTicks.collect { tickMap ->
                tickMap.forEach { (symbol, tick) ->
                    ctraderRepository.updatePositionsPrice(symbol, tick.currentPrice)
                }
                evaluateAlertTriggers(allSignals.value, tickMap)
            }
        }
    }

    private fun startAutoRefreshLoop() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                if (_uiState.value.autoRefreshEnabled) {
                    for (sec in 15 downTo 1) {
                        _uiState.value = _uiState.value.copy(countdownSeconds = sec)
                        delay(1000L)
                    }
                    _uiState.value = _uiState.value.copy(countdownSeconds = 0)
                    refreshData(isSilent = true)
                } else {
                    delay(2000L)
                }
            }
        }
    }

    fun refreshData(isSilent: Boolean = false) {
        viewModelScope.launch {
            if (!isSilent) {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
            }
            repository.refreshData()
            calendarRepository.refreshCalendar(force = !isSilent)
            if (!isSilent) {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
            // If a signal is currently open in detail, update it
            val curSelected = _uiState.value.selectedSignal
            if (curSelected != null) {
                val updated = repository.signals.value.find { it.symbol == curSelected.symbol }
                if (updated != null) {
                    _uiState.value = _uiState.value.copy(selectedSignal = updated)
                }
            }
        }
    }

    fun refreshCalendar(force: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            calendarRepository.refreshCalendar(force = force)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
            val stat = calendarRepository.status.value
            if (stat.isRateLimited) {
                setToast("Forex Factory cooldown active: Showing cached schedule")
            } else if (stat.isLive) {
                setToast("Forex Factory Live Calendar updated (${stat.totalEvents} events)")
            } else {
                setToast("Forex Factory Calendar ready (${stat.totalEvents} events)")
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun setAssetClassFilter(assetClass: AssetClass) {
        _uiState.value = _uiState.value.copy(selectedAssetClass = assetClass)
    }

    fun setSctArchetypeFilter(archetype: com.example.data.model.SctStrategyArchetype) {
        _uiState.value = _uiState.value.copy(selectedSctArchetype = archetype)
    }

    fun setDirectionFilter(direction: String) {
        _uiState.value = _uiState.value.copy(selectedDirectionFilter = direction)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun openSignalDetail(signal: TradingSignal) {
        _uiState.value = _uiState.value.copy(selectedSignal = signal)
        recalculateRiskForSignal(signal)
    }

    fun closeSignalDetail() {
        _uiState.value = _uiState.value.copy(selectedSignal = null)
    }

    fun changeDetailTimeframe(timeframe: String) {
        val cur = _uiState.value.selectedSignal ?: return
        _uiState.value = _uiState.value.copy(selectedDetailTimeframe = timeframe)
        viewModelScope.launch {
            val candles = repository.getCandlesForSymbol(cur.symbol, timeframe)
            _uiState.value = _uiState.value.copy(
                selectedSignal = cur.copy(candles = candles, timeframe = timeframe)
            )
        }
    }

    fun toggleBookmark(symbol: String) {
        val isSaved = repository.toggleBookmark(symbol)
        val curSelected = _uiState.value.selectedSignal
        if (curSelected?.symbol == symbol) {
            _uiState.value = _uiState.value.copy(
                selectedSignal = curSelected.copy(isBookmarked = isSaved)
            )
        }
    }

    fun toggleAutoRefresh() {
        val next = !_uiState.value.autoRefreshEnabled
        _uiState.value = _uiState.value.copy(autoRefreshEnabled = next)
    }

    fun showRiskCalculator(show: Boolean) {
        _uiState.value = _uiState.value.copy(showRiskCalculator = show)
        if (show) {
            val signal = _uiState.value.selectedSignal ?: filteredSignals.value.firstOrNull()
            if (signal != null) {
                recalculateRiskForSignal(signal)
            }
        }
    }

    fun showDiagnostics(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDiagnostics = show)
    }

    fun updateCalcBalance(balanceStr: String) {
        _calcBalance.value = balanceStr
        val signal = _uiState.value.selectedSignal ?: filteredSignals.value.firstOrNull()
        if (signal != null) recalculateRiskForSignal(signal)
    }

    fun updateCalcRiskPercent(pctStr: String) {
        _calcRiskPercent.value = pctStr
        val signal = _uiState.value.selectedSignal ?: filteredSignals.value.firstOrNull()
        if (signal != null) recalculateRiskForSignal(signal)
    }

    private fun recalculateRiskForSignal(signal: TradingSignal) {
        val bal = _calcBalance.value.toDoubleOrNull() ?: 10000.0
        val riskPct = _calcRiskPercent.value.toDoubleOrNull() ?: 1.5
        val result = repository.calculateRisk(
            accountBalance = bal,
            riskPercent = riskPct,
            entryPrice = signal.entryPrice,
            stopLoss = signal.stopLoss,
            tp1 = signal.tp1,
            tp2 = signal.tp2,
            assetClass = signal.assetClass
        )
        _calcResult.value = result
    }

    fun updateSctSettings(settings: com.example.data.model.SctSettings) {
        repository.updateSctSettings(settings)
    }

    fun resetSctSettings() {
        repository.resetSctSettings()
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun setToast(message: String) {
        _uiState.value = _uiState.value.copy(toastMessage = message)
    }

    fun showTimeZoneDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTimeZoneDialog = show)
    }

    fun setTimeZone(tz: TimeZoneSetting) {
        _selectedTimeZone.value = tz
        _uiState.value = _uiState.value.copy(selectedTimeZone = tz)
        repository.saveTimeZone(tz.id)
    }

    fun showPairSelectionDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPairSelectionDialog = show)
    }

    fun updateSelectedPairs(pairs: Set<String>) {
        _selectedPairs.value = pairs
        repository.saveSelectedPairs(pairs)
        repository.setOnboardingCompleted(true)
        _uiState.value = _uiState.value.copy(
            showPairSelectionDialog = false,
            isFirstLaunch = false
        )
        setToast("Subscribed to ${pairs.size} signal pairs")
    }

    fun toggleFilterOnlySubscribedPairs() {
        val next = !_uiState.value.filterOnlySubscribedPairs
        _uiState.value = _uiState.value.copy(filterOnlySubscribedPairs = next)
        setToast(if (next) "Filtered: Subscribed pairs" else "Showing all market pairs")
    }

    fun openNinjaSettings(open: Boolean) {
        _uiState.value = _uiState.value.copy(showNinjaSettingsDialog = open)
    }

    fun updateNinjaSettings(ninja: com.example.data.model.NinjaSettings) {
        val current = sctSettings.value
        updateSctSettings(current.copy(ninjaSettings = ninja))
        setToast("Ninja USDJPY Strategy updated")
    }

    fun toggleNinja(enabled: Boolean? = null) {
        val current = sctSettings.value
        val next = enabled ?: !current.ninjaSettings.enabled
        val updatedNinja = current.ninjaSettings.copy(enabled = next)
        updateSctSettings(current.copy(ninjaSettings = updatedNinja))
        setToast(if (next) "Ninja Strategy ACTIVATED" else "Ninja Strategy Standby")
    }

    fun openDxTradeConnect(open: Boolean) {
        _uiState.value = _uiState.value.copy(showDxTradeConnectDialog = open)
    }

    fun openDxTradeOrder(signal: TradingSignal? = null) {
        _uiState.value = _uiState.value.copy(
            showDxTradeOrderDialog = true,
            tradeTargetSignal = signal ?: _uiState.value.selectedSignal ?: filteredSignals.value.firstOrNull()
        )
    }

    fun closeDxTradeOrder() {
        _uiState.value = _uiState.value.copy(
            showDxTradeOrderDialog = false,
            tradeTargetSignal = null
        )
    }

    fun openDxTradePositions(open: Boolean) {
        _uiState.value = _uiState.value.copy(showDxTradePositionsDialog = open)
    }

    fun connectDxTrade(credentials: com.example.data.model.DxTradeCredentials) {
        viewModelScope.launch {
            dxTradeRepository.connect(credentials)
            setToast("Connecting to DXtrade…")
        }
    }

    fun connectDxTradeDemo() {
        viewModelScope.launch {
            dxTradeRepository.connectDemo()
            setToast("Connected to DXtrade Demo")
        }
    }

    fun disconnectDxTrade() {
        dxTradeRepository.disconnect()
        setToast("Disconnected from DXtrade")
    }

    fun executeDxTradeOrder(req: com.example.data.model.DxTradeOrderRequest, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = dxTradeRepository.submitOrder(req)
            if (res.isSuccess) {
                setToast("DXtrade Order #${res.getOrNull()?.orderId} Executed!")
                onResult(true, res.getOrNull()?.orderId ?: "OK")
            } else {
                setToast("DXtrade execution error")
                onResult(false, res.exceptionOrNull()?.message ?: "Failed")
            }
        }
    }

    fun closeDxTradePosition(posId: String) {
        viewModelScope.launch {
            dxTradeRepository.closePosition(posId)
        }
    }

    fun selectPerformanceSymbol(symbol: String) {
        _selectedPerformanceSymbol.value = symbol
    }

    fun getPerformanceForSymbol(symbol: String): PairFiveDayPerformance {
        val sig = allSignals.value.find { it.symbol == symbol }
        val tick = allTicks.value[symbol]
        val name = sig?.name ?: symbol
        val assetClass = sig?.assetClass ?: AssetClass.FOREX
        val price = tick?.currentPrice ?: sig?.currentPrice ?: 1.0
        return SctPerformanceEngine.computeFiveDayPerformance(symbol, name, assetClass, price)
    }

    fun navigateToPerformance(symbol: String) {
        selectPerformanceSymbol(symbol)
        _uiState.value = _uiState.value.copy(
            selectedSignal = null,
            selectedTab = 5
        )
    }

    fun openCTraderTerminal(open: Boolean) {
        _uiState.value = _uiState.value.copy(showCTraderTerminalDialog = open)
    }

    fun openCTraderOrder(signal: TradingSignal? = null) {
        _uiState.value = _uiState.value.copy(
            showCTraderOrderDialog = true,
            ctraderTargetSignal = signal ?: _uiState.value.selectedSignal ?: filteredSignals.value.firstOrNull()
        )
    }

    fun closeCTraderOrder() {
        _uiState.value = _uiState.value.copy(showCTraderOrderDialog = false, ctraderTargetSignal = null)
    }

    fun handleCTraderAuthCode(code: String) {
        viewModelScope.launch {
            setToast("Exchanging Spotware OAuth Token…")
            val res = ctraderRepository.handleAuthCode(code)
            if (res.isSuccess) {
                setToast(res.getOrNull() ?: "cTrader Connected!")
            } else {
                setToast("cTrader Auth Error: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun saveCTraderCredentials(clientId: String, clientSecret: String, redirectUri: String) {
        ctraderRepository.saveCredentials(clientId, clientSecret, redirectUri)
        setToast("cTrader Open API credentials saved")
    }

    fun selectCTraderAccount(account: com.example.data.model.CTraderAccount) {
        ctraderRepository.selectAccount(account)
        setToast("Active: ${account.brokerName} (#${account.accountId})")
    }

    fun connectCTraderDemo() {
        ctraderRepository.connectDemoAccount()
        setToast("Connected to Spotware cTrader Demo Sandbox ($50k)")
    }

    fun disconnectCTrader() {
        ctraderRepository.disconnect()
        setToast("Disconnected from cTrader")
    }

    fun executeCTraderOrder(req: com.example.data.model.CTraderOrderRequest) {
        val result = ctraderRepository.executeOrder(req)
        if (result.isSuccess) {
            setToast(result.message)
            closeCTraderOrder()
        } else {
            setToast(result.message)
        }
    }

    fun closeCTraderPosition(positionId: Long) {
        val ok = ctraderRepository.closePosition(positionId)
        if (ok) setToast("cTrader position #$positionId closed")
    }

    fun closeAllCTraderPositions() {
        val count = ctraderRepository.closeAllPositions()
        setToast("Closed $count cTrader positions")
    }

    // --- NOTIFICATION ALERTS & SLIDESHOW CONTROLS ---

    fun showNotificationAlertsDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showNotificationAlertsDialog = show)
        if (show) {
            _unreadAlertsCount.value = 0
        }
    }

    fun showFullSlideshowDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFullSlideshowDialog = show)
    }

    fun toggleSlideshowAutoPlay() {
        _uiState.value = _uiState.value.copy(slideshowAutoPlay = !_uiState.value.slideshowAutoPlay)
    }

    fun toggleAlerts(enabled: Boolean) {
        _alertsEnabled.value = enabled
        repository.saveAlertsEnabled(enabled)
        _uiState.value = _uiState.value.copy(alertsEnabled = enabled)
        setToast(if (enabled) "Signal notifications active for selected pairs" else "Signal notifications muted")
    }

    fun toggleNinjaAlerts(enabled: Boolean) {
        _ninjaAlertsEnabled.value = enabled
        repository.saveNinjaAlertsEnabled(enabled)
        _uiState.value = _uiState.value.copy(ninjaAlertsEnabled = enabled)
        setToast(if (enabled) "Ninja Mode alerts enabled (USDJPY)" else "Ninja Mode alerts muted")
    }

    fun clearTriggerAlerts() {
        _triggerAlerts.value = emptyList()
        _unreadAlertsCount.value = 0
        alertedTriggerKeys.clear()
        setToast("Cleared alert history")
    }

    fun simulateTestTriggerAlert() {
        val isNinja = sctSettings.value.ninjaSettings.enabled
        val symbol = if (isNinja) "USDJPY" else (_selectedPairs.value.firstOrNull() ?: "EURUSD")
        val alert = if (isNinja) {
            SctTriggerAlert(
                symbol = "USDJPY",
                triggerType = TriggerType.NINJA_SC_ZONE_BREAKOUT,
                isBullish = true,
                detail = "TEST ALERT: Ninja USDJPY SC Zone 09-11 Breakout validated! 2 M5 Closes above 154.200. TP Hunter active.",
                isNinjaAlert = true
            )
        } else {
            SctTriggerAlert(
                symbol = symbol,
                triggerType = TriggerType.EMA_BULLISH_CROSSOVER,
                isBullish = true,
                detail = "TEST ALERT: $symbol EMA 9 crossed EMA 21 on M5. Retest wick entry confirmed @ live market price.",
                isNinjaAlert = false
            )
        }

        _triggerAlerts.value = (listOf(alert) + _triggerAlerts.value).take(50)
        _unreadAlertsCount.value += 1
        SctNotificationHelper.sendNotification(getApplication(), alert)
        setToast("Pushed notification alert for $symbol!")
    }

    fun evaluateAlertTriggers(signals: List<TradingSignal>, ticks: Map<String, BiquoteTick>) {
        if (!_alertsEnabled.value || signals.isEmpty()) return

        val subscribed = _selectedPairs.value
        val isNinjaEnabled = sctSettings.value.ninjaSettings.enabled
        val ninjaAlerts = _ninjaAlertsEnabled.value

        val newAlerts = mutableListOf<SctTriggerAlert>()

        // 1. Ninja Mode USDJPY Institutional Trigger Check
        if (isNinjaEnabled && ninjaAlerts) {
            val usdjpySignal = signals.find { it.symbol == "USDJPY" }
            if (usdjpySignal != null) {
                val tick = ticks["USDJPY"]
                val price = tick?.currentPrice ?: usdjpySignal.currentPrice
                val ninja = sctSettings.value.ninjaSettings

                if (usdjpySignal.signalType == SignalType.STRONG_BUY || usdjpySignal.signalType == SignalType.BUY) {
                    val key = "USDJPY_NINJA_BUY_${usdjpySignal.sctLifecycle}"
                    if (!alertedTriggerKeys.contains(key)) {
                        alertedTriggerKeys.add(key)
                        val alert = SctTriggerAlert(
                            symbol = "USDJPY",
                            triggerType = TriggerType.NINJA_SC_ZONE_BREAKOUT,
                            isBullish = true,
                            detail = "Ninja USDJPY confirmed 2 M5 closes above SC Zone High. Demand OB active @ ${String.format("%.3f", price)}. Target: ${String.format("%.3f", usdjpySignal.tp1)}.",
                            isNinjaAlert = true
                        )
                        newAlerts.add(alert)
                    }
                } else if (usdjpySignal.signalType == SignalType.STRONG_SELL || usdjpySignal.signalType == SignalType.SELL) {
                    val key = "USDJPY_NINJA_SELL_${usdjpySignal.sctLifecycle}"
                    if (!alertedTriggerKeys.contains(key)) {
                        alertedTriggerKeys.add(key)
                        val alert = SctTriggerAlert(
                            symbol = "USDJPY",
                            triggerType = TriggerType.NINJA_SC_ZONE_BREAKOUT,
                            isBullish = false,
                            detail = "Ninja USDJPY confirmed 2 M5 closes below SC Zone Low. Supply OB active @ ${String.format("%.3f", price)}. Target: ${String.format("%.3f", usdjpySignal.tp1)}.",
                            isNinjaAlert = true
                        )
                        newAlerts.add(alert)
                    }
                }
            }
        }

        // 2. Selected Pairs SCT Theory Triggers Check
        val monitoredSignals = signals.filter { subscribed.isEmpty() || it.symbol in subscribed }
        for (signal in monitoredSignals) {
            val tick = ticks[signal.symbol]
            val price = tick?.currentPrice ?: signal.currentPrice
            val sct = signal.sctLevels ?: continue

            // EMA Cross
            if (sct.ema9 > 0.0 && sct.ema21 > 0.0) {
                if (sct.ema9 > sct.ema21 && signal.signalType.isBullish) {
                    val key = "${signal.symbol}_EMA_BULL"
                    if (!alertedTriggerKeys.contains(key)) {
                        alertedTriggerKeys.add(key)
                        newAlerts.add(
                            SctTriggerAlert(
                                symbol = signal.symbol,
                                triggerType = TriggerType.EMA_BULLISH_CROSSOVER,
                                isBullish = true,
                                detail = "${signal.symbol} EMA 9 crossed above EMA 21 ribbon. Institutional buy setup @ ${String.format("%.4f", price)}."
                            )
                        )
                    }
                } else if (sct.ema9 < sct.ema21 && signal.signalType.isBearish) {
                    val key = "${signal.symbol}_EMA_BEAR"
                    if (!alertedTriggerKeys.contains(key)) {
                        alertedTriggerKeys.add(key)
                        newAlerts.add(
                            SctTriggerAlert(
                                symbol = signal.symbol,
                                triggerType = TriggerType.EMA_BEARISH_CROSSOVER,
                                isBullish = false,
                                detail = "${signal.symbol} EMA 9 crossed below EMA 21 ribbon. Institutional sell setup @ ${String.format("%.4f", price)}."
                            )
                        )
                    }
                }
            }

            // Asian Range Breakout
            if (sct.asianHigh > 0.0 && sct.asianLow > 0.0) {
                if (price > sct.asianHigh) {
                    val key = "${signal.symbol}_ASIAN_BULL"
                    if (!alertedTriggerKeys.contains(key)) {
                        alertedTriggerKeys.add(key)
                        newAlerts.add(
                            SctTriggerAlert(
                                symbol = signal.symbol,
                                triggerType = TriggerType.ASIAN_RANGE_BREAKOUT,
                                isBullish = true,
                                detail = "${signal.symbol} Asian Range Breakout confirmed! Price @ ${String.format("%.4f", price)}, targeting PDH (${String.format("%.4f", sct.pdh)})."
                            )
                        )
                    }
                } else if (price < sct.asianLow) {
                    val key = "${signal.symbol}_ASIAN_BEAR"
                    if (!alertedTriggerKeys.contains(key)) {
                        alertedTriggerKeys.add(key)
                        newAlerts.add(
                            SctTriggerAlert(
                                symbol = signal.symbol,
                                triggerType = TriggerType.ASIAN_RANGE_BREAKOUT,
                                isBullish = false,
                                detail = "${signal.symbol} Asian Range Breakdown confirmed! Price @ ${String.format("%.4f", price)}, targeting PDL (${String.format("%.4f", sct.pdl)})."
                            )
                        )
                    }
                }
            }
        }

        if (newAlerts.isNotEmpty()) {
            _triggerAlerts.value = (newAlerts + _triggerAlerts.value).take(50)
            _unreadAlertsCount.value += newAlerts.size
            newAlerts.forEach { alert ->
                SctNotificationHelper.sendNotification(getApplication(), alert)
            }
        }
    }
}
