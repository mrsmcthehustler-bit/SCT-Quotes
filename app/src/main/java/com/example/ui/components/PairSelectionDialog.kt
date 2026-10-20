package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AssetClass
import com.example.data.model.TradingSignal
import com.example.data.repository.InstrumentInfo
import com.example.data.repository.TradingSignalEngine
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun PairSelectionDialog(
    allInstruments: List<InstrumentInfo>,
    selectedPairs: Set<String>,
    currentSignals: List<TradingSignal> = emptyList(),
    onSaveSelection: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    isOnboarding: Boolean = false,
    initialTab: Int = 0
) {
    var currentSelection by remember(selectedPairs) { mutableStateOf(selectedPairs.toMutableSet()) }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(initialTab) } // 0: Select Pairs, 1: Signals Slideshow
    var activeSlideIndex by remember { mutableIntStateOf(0) }
    var isAutoPlaying by remember { mutableStateOf(true) }

    val categories = listOf(
        "ALL" to "All Pairs",
        "FOREX" to "Forex Majors",
        "METALS" to "Metals",
        "CRYPTO" to "Crypto",
        "INDICES" to "Indices"
    )

    val coreFive = setOf("EURUSD", "GBPUSD", "USDJPY", "USDCAD", "XAUUSD")

    // Filter signals matching currently selected pairs
    val selectedSignals = remember(currentSignals, currentSelection, allInstruments) {
        val matching = currentSignals.filter { it.symbol in currentSelection }
        if (matching.isNotEmpty()) {
            matching
        } else {
            // Fallback: match coreFive or first signals
            currentSignals.filter { it.symbol in coreFive }.ifEmpty { currentSignals.take(5) }
        }
    }

    // Adjust active index when list size changes
    LaunchedEffect(selectedSignals.size) {
        if (selectedSignals.isNotEmpty()) {
            activeSlideIndex = activeSlideIndex.coerceIn(0, selectedSignals.size - 1)
        } else {
            activeSlideIndex = 0
        }
    }

    // Auto-advance slideshow
    LaunchedEffect(isAutoPlaying, selectedSignals.size, activeSlideIndex) {
        if (isAutoPlaying && selectedSignals.size > 1) {
            delay(3500L)
            activeSlideIndex = (activeSlideIndex + 1) % selectedSignals.size
        }
    }

    val filteredInstruments = remember(allInstruments, selectedCategory, searchQuery) {
        allInstruments.filter { inst ->
            val matchesCat = when (selectedCategory) {
                "FOREX" -> inst.assetClass == AssetClass.FOREX
                "METALS" -> inst.assetClass == AssetClass.METALS
                "CRYPTO" -> inst.assetClass == AssetClass.CRYPTO
                "INDICES" -> inst.assetClass == AssetClass.INDICES
                else -> true
            }
            val matchesQuery = searchQuery.isEmpty() ||
                    inst.symbol.contains(searchQuery, ignoreCase = true) ||
                    inst.name.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isOnboarding) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isOnboarding,
            dismissOnClickOutside = !isOnboarding,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("pair_selection_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NeonCyan.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Default.Slideshow else Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (isOnboarding) "Welcome to SCT Quotes" else "Signal Pair Subscriptions",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isOnboarding) {
                                    "Select pairs to activate live SCT institutional signals & slideshow"
                                } else {
                                    "Configure subscribed pairs and preview signals"
                                },
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (!isOnboarding) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp).testTag("close_pair_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs: Pairs Checklist vs Signals Slideshow
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = TerminalSurfaceElevated,
                    contentColor = NeonCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonCyan,
                            height = 2.dp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(0.5.dp, TerminalBorder, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (selectedTab == 0) NeonCyan else TextSecondary
                                )
                                Text(
                                    text = "Select Pairs (${currentSelection.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) NeonCyan else TextSecondary
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_select_pairs")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Slideshow,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (selectedTab == 1) NeonCyan else TextSecondary
                                )
                                Text(
                                    text = "Signals Slideshow",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) NeonCyan else TextSecondary
                                )
                                if (selectedSignals.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .background(BullishGreen, CircleShape)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "${selectedSignals.size}",
                                            color = TerminalDark,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_signals_slideshow")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> {
                            // TAB 0: Pair Selection with Slideshow Preview Banner
                            PairSelectionTabContent(
                                allInstruments = allInstruments,
                                filteredInstruments = filteredInstruments,
                                currentSelection = currentSelection,
                                categories = categories,
                                selectedCategory = selectedCategory,
                                searchQuery = searchQuery,
                                coreFive = coreFive,
                                selectedSignals = selectedSignals,
                                activeSlideIndex = activeSlideIndex,
                                onSelectionChange = { currentSelection = it },
                                onCategoryChange = { selectedCategory = it },
                                onSearchChange = { searchQuery = it },
                                onOpenSlideshow = { selectedTab = 1 }
                            )
                        }
                        1 -> {
                            // TAB 1: Full Immersive Signal Slideshow
                            SignalSlideshowTabContent(
                                selectedSignals = selectedSignals,
                                currentSelection = currentSelection,
                                activeSlideIndex = activeSlideIndex,
                                isAutoPlaying = isAutoPlaying,
                                onSlideChange = { activeSlideIndex = it },
                                onToggleAutoPlay = { isAutoPlaying = !isAutoPlaying },
                                onSwitchToSelection = { selectedTab = 0 }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Button (Confirm & Proceed)
                Button(
                    onClick = {
                        val finalSelection = if (currentSelection.isEmpty()) {
                            coreFive
                        } else {
                            currentSelection
                        }
                        onSaveSelection(finalSelection)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_pair_selection_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isOnboarding) {
                                "Confirm ${currentSelection.size} Pairs & Enter Terminal"
                            } else {
                                "Save Signal Preferences (${currentSelection.size})"
                            },
                            color = TerminalDark,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TerminalDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PairSelectionTabContent(
    allInstruments: List<InstrumentInfo>,
    filteredInstruments: List<InstrumentInfo>,
    currentSelection: Set<String>,
    categories: List<Pair<String, String>>,
    selectedCategory: String,
    searchQuery: String,
    coreFive: Set<String>,
    selectedSignals: List<TradingSignal>,
    activeSlideIndex: Int,
    onSelectionChange: (MutableSet<String>) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenSlideshow: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Compact Live Signals Slideshow Preview Banner
        if (selectedSignals.isNotEmpty()) {
            val previewSignal = selectedSignals.getOrNull(activeSlideIndex) ?: selectedSignals.first()
            val isBullish = previewSignal.signalType.isBullish

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenSlideshow)
                    .testTag("slideshow_preview_banner"),
                color = TerminalSurfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(if (isBullish) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBullish) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isBullish) BullishGreen else BearishRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = previewSignal.symbol,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Box(
                                    modifier = Modifier
                                        .background(if (isBullish) BullishGreen else BearishRed, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (isBullish) "BUY" else "SELL",
                                        color = TerminalDark,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${previewSignal.confidenceScore}% win rate",
                                    fontSize = 10.sp,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${previewSignal.sctStrategyType} • TP1: ${TradingSignalEngine.formatPrice(previewSignal.tp1, previewSignal.symbol)}",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Slideshow (${activeSlideIndex + 1}/${selectedSignals.size})",
                            fontSize = 10.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Search input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search pairs (e.g. EUR, XAU, BTC)…", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = TerminalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = TerminalDark,
                unfocusedContainerColor = TerminalDark
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("pair_search_field")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Preset Action Chips: SCT Core 5, Select All, Clear
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {
                    onSelectionChange((currentSelection + coreFive).toMutableSet())
                },
                label = { Text("SCT Core 5", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = TerminalSurfaceElevated,
                    labelColor = GoldAccent
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                modifier = Modifier.testTag("preset_core5")
            )

            AssistChip(
                onClick = {
                    onSelectionChange(allInstruments.map { it.symbol }.toMutableSet())
                },
                label = { Text("Select All", fontSize = 10.sp) },
                leadingIcon = {
                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = TerminalSurfaceElevated,
                    labelColor = NeonCyan
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.testTag("preset_select_all")
            )

            AssistChip(
                onClick = { onSelectionChange(mutableSetOf()) },
                label = { Text("Clear", fontSize = 10.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = TerminalSurfaceElevated,
                    labelColor = TextMuted
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.testTag("preset_clear")
            )
        }

        // Category pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { (key, label) ->
                val isSelected = selectedCategory == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) NeutralBlue else TerminalSurfaceElevated)
                        .border(
                            0.5.dp,
                            if (isSelected) NeutralBlue else TerminalBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onCategoryChange(key) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = if (isSelected) Color(0xFF090D16) else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Selected count indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Instruments",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${currentSelection.size} of ${allInstruments.size} Selected",
                fontSize = 11.sp,
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Instrument List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredInstruments, key = { it.symbol }) { instrument ->
                val isSelected = currentSelection.contains(instrument.symbol)
                PairItemCard(
                    instrument = instrument,
                    isSelected = isSelected,
                    onToggle = {
                        val next = currentSelection.toMutableSet()
                        if (isSelected) next.remove(instrument.symbol) else next.add(instrument.symbol)
                        onSelectionChange(next)
                    }
                )
            }
        }
    }
}

@Composable
private fun SignalSlideshowTabContent(
    selectedSignals: List<TradingSignal>,
    currentSelection: Set<String>,
    activeSlideIndex: Int,
    isAutoPlaying: Boolean,
    onSlideChange: (Int) -> Unit,
    onToggleAutoPlay: () -> Unit,
    onSwitchToSelection: () -> Unit
) {
    if (selectedSignals.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Slideshow,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No Pairs Selected for Slideshow",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select one or more currency pairs, commodities, or indices to preview their signals in the slideshow.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onSwitchToSelection,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Select Trading Pairs", color = TerminalDark, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val activeSignal = selectedSignals[activeSlideIndex.coerceIn(0, selectedSignals.size - 1)]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("signals_slideshow_container")
    ) {
        // Slideshow Top Controls Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp)),
            color = TerminalSurfaceElevated
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev button
                IconButton(
                    onClick = {
                        val prev = if (activeSlideIndex > 0) activeSlideIndex - 1 else selectedSignals.size - 1
                        onSlideChange(prev)
                    },
                    modifier = Modifier.size(32.dp).testTag("slideshow_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Slide",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Center Slide Counter & Autoplay badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SLIDE ${activeSlideIndex + 1} OF ${selectedSignals.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )

                    // Autoplay toggle button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.5.dp, if (isAutoPlaying) BullishGreen else TextMuted, RoundedCornerShape(12.dp))
                            .clickable(onClick = onToggleAutoPlay)
                            .testTag("slideshow_autoplay_toggle"),
                        color = TerminalDark,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (isAutoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isAutoPlaying) "Pause" else "Play",
                                tint = if (isAutoPlaying) BullishGreen else TextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = if (isAutoPlaying) "Auto" else "Paused",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAutoPlaying) BullishGreen else TextMuted
                            )
                        }
                    }
                }

                // Next button
                IconButton(
                    onClick = {
                        val next = (activeSlideIndex + 1) % selectedSignals.size
                        onSlideChange(next)
                    },
                    modifier = Modifier.size(32.dp).testTag("slideshow_next_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Slide",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Pairs Quick-Jump Ribbon
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(selectedSignals.indices.toList()) { index ->
                val sig = selectedSignals[index]
                val isActive = index == activeSlideIndex
                val isBull = sig.signalType.isBullish

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.dp,
                            if (isActive) NeonCyan else TerminalBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSlideChange(index) }
                        .testTag("slideshow_chip_${sig.symbol}"),
                    color = if (isActive) NeonCyan.copy(alpha = 0.15f) else TerminalSurfaceElevated,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isBull) BullishGreen else BearishRed, CircleShape)
                        )
                        Text(
                            text = sig.symbol,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                            color = if (isActive) NeonCyan else TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active Slide Card (Scrollable for full level details)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ActiveSignalSlideDetailCard(signal = activeSignal)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Slide Indicator Dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            selectedSignals.forEachIndexed { idx, _ ->
                val isCurrent = idx == activeSlideIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (isCurrent) 20.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent) NeonCyan else TerminalBorder)
                        .clickable { onSlideChange(idx) }
                )
            }
        }
    }
}

@Composable
private fun ActiveSignalSlideDetailCard(signal: TradingSignal) {
    val isBullish = signal.signalType.isBullish
    val accentColor = if (isBullish) BullishGreen else BearishRed

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("active_slide_card"),
        color = TerminalSurfaceCard,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Slide Header: Symbol, Asset Class, Direction & Win Probability
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
                            .size(36.dp)
                            .background(accentColor.copy(alpha = 0.18f), CircleShape)
                            .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBullish) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = signal.symbol,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .background(TerminalDark, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, TerminalBorder, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = signal.assetClass.displayName.uppercase(),
                                    color = NeutralBlue,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = signal.name,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Direction Badge
                Box(
                    modifier = Modifier
                        .background(accentColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isBullish) "BULLISH BUY" else "BEARISH SELL",
                        color = TerminalDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Strategy Archetype & Confidence Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                color = TerminalSurfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SCT Strategy Archetype",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                        Text(
                            text = signal.sctStrategyType,
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Confidence Score",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${signal.confidenceScore}% WIN RATE",
                            color = BullishGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4-Block Key Trading Levels Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LevelMetricBox(
                    label = "ENTRY PRICE",
                    value = TradingSignalEngine.formatPrice(signal.entryPrice, signal.symbol),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                LevelMetricBox(
                    label = "CURRENT PRICE",
                    value = TradingSignalEngine.formatPrice(signal.currentPrice, signal.symbol),
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LevelMetricBox(
                    label = "STOP LOSS",
                    value = TradingSignalEngine.formatPrice(signal.stopLoss, signal.symbol),
                    color = BearishRed,
                    modifier = Modifier.weight(1f)
                )
                LevelMetricBox(
                    label = "TAKE PROFIT (TP1)",
                    value = TradingSignalEngine.formatPrice(signal.tp1, signal.symbol),
                    color = BullishGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SCT Levels Overview (Asian Range & Retest)
            signal.sctLevels?.let { levels ->
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
                        Column {
                            Text("Asian Range Box", color = TextMuted, fontSize = 9.sp)
                            Text(
                                "${TradingSignalEngine.formatPrice(levels.asianLow, signal.symbol)} - ${TradingSignalEngine.formatPrice(levels.asianHigh, signal.symbol)}",
                                color = Color(0xFFA78BFA),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Retest Level", color = TextMuted, fontSize = 9.sp)
                            Text(
                                TradingSignalEngine.formatPrice(levels.retestLevel ?: levels.rangeHigh, signal.symbol),
                                color = NeutralBlue,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Risk / Reward", color = TextMuted, fontSize = 9.sp)
                            Text(
                                "1:${String.format(Locale.US, "%.1f", signal.riskRewardRatio)}",
                                color = GoldAccent,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Institutional Rationale Box
            if (signal.rationale.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(0.5.dp, TerminalBorder, RoundedCornerShape(8.dp)),
                    color = TerminalDark
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Institutional Setup Commentary",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = signal.rationale,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelMetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, TerminalBorder, RoundedCornerShape(8.dp)),
        color = TerminalSurfaceElevated
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                color = color,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun PairItemCard(
    instrument: InstrumentInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val borderColor = if (isSelected) NeonCyan.copy(alpha = 0.7f) else TerminalBorder
    val bgColor = if (isSelected) TerminalSurfaceCard else TerminalSurfaceElevated.copy(alpha = 0.5f)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .testTag("pair_item_${instrument.symbol}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Checkbox indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) NeonCyan else Color.Transparent)
                        .border(
                            1.5.dp,
                            if (isSelected) NeonCyan else TextMuted,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = TerminalDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = instrument.symbol,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .background(TerminalDark, RoundedCornerShape(4.dp))
                                .border(0.5.dp, TerminalBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = instrument.assetClass.displayName.uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeutralBlue
                            )
                        }
                    }
                    Text(
                        text = instrument.name,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Default rate preview
            Text(
                text = String.format(Locale.US, if (instrument.defaultPrice > 500) "%.1f" else "%.4f", instrument.defaultPrice),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
            )
        }
    }
}
