package com.example.ui.screens

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.EconomicEvent
import com.example.data.model.NewsImpact
import com.example.data.model.SupportedTimeZones
import com.example.data.model.TimeZoneSetting
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun EconomicCalendarScreen(
    viewModel: MainViewModel,
    onSelectPair: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val events by viewModel.economicEvents.collectAsState()
    val timeZone by viewModel.selectedTimeZone.collectAsState()
    val calendarStatus by viewModel.calendarStatus.collectAsState()
    val nextHighImpact by viewModel.nextHighImpactEvent.collectAsState()

    var impactFilter by remember { mutableStateOf<NewsImpact?>(null) } // null = ALL
    var currencyFilter by remember { mutableStateOf("ALL") }
    var dayFilter by remember { mutableStateOf("ALL") } // "ALL", "TODAY", "TOMORROW", "UPCOMING"
    var searchQuery by remember { mutableStateOf("") }
    var selectedEventForDetail by remember { mutableStateOf<EconomicEvent?>(null) }

    val currencies = listOf("ALL", "USD", "EUR", "GBP", "JPY", "CAD", "CHF", "AUD", "NZD")
    val nowMs = System.currentTimeMillis()

    val filteredEvents = remember(events, impactFilter, currencyFilter, dayFilter, searchQuery, timeZone) {
        events.filter { event ->
            val matchesImpact = impactFilter == null || event.impact == impactFilter
            val matchesCurrency = currencyFilter == "ALL" || event.country == currencyFilter
            val matchesDay = when (dayFilter) {
                "TODAY" -> event.isToday(timeZone, nowMs)
                "TOMORROW" -> event.isTomorrow(timeZone, nowMs)
                "UPCOMING" -> event.utcTimestamp > nowMs
                else -> true
            }
            val matchesSearch = searchQuery.isEmpty() ||
                    event.title.contains(searchQuery, ignoreCase = true) ||
                    event.country.contains(searchQuery, ignoreCase = true)
            matchesImpact && matchesCurrency && matchesDay && matchesSearch
        }.sortedBy { it.utcTimestamp }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalDark)
            .testTag("economic_calendar_screen")
    ) {
        // Top Header Row with Forex Factory Brand & Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Forex Factory®",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    // Status Badge
                    val statusBg = when {
                        calendarStatus.isLive -> BullishGreen.copy(alpha = 0.2f)
                        calendarStatus.isRateLimited -> WarningAmber.copy(alpha = 0.2f)
                        else -> NeutralBlue.copy(alpha = 0.2f)
                    }
                    val statusTextColor = when {
                        calendarStatus.isLive -> BullishGreen
                        calendarStatus.isRateLimited -> WarningAmber
                        else -> NeutralBlue
                    }
                    val statusText = when {
                        calendarStatus.isLive -> "LIVE FEED"
                        calendarStatus.isRateLimited -> "CACHED (429)"
                        else -> "API FEED"
                    }

                    Box(
                        modifier = Modifier
                            .background(statusBg, RoundedCornerShape(4.dp))
                            .border(0.5.dp, statusTextColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = statusTextColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Macro Releases & SCT Volatility Playbook",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Refresh Button
                IconButton(
                    onClick = { viewModel.refreshCalendar(force = true) },
                    modifier = Modifier.size(32.dp).testTag("refresh_calendar_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Calendar",
                        tint = NeutralBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Interactive Time Zone Switcher Pill
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, NeutralBlue, RoundedCornerShape(20.dp))
                        .clickable { viewModel.showTimeZoneDialog(true) }
                        .testTag("timezone_switch_button"),
                    color = TerminalSurfaceElevated,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Switch Time Zone",
                            tint = NeutralBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${timeZone.abbreviation} (${if (timeZone.offsetHours >= 0) "+" else ""}${timeZone.offsetHours}h)",
                            color = NeutralBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "▾",
                            color = NeutralBlue,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // High-Impact Catalyst Banner (If upcoming)
        if (nextHighImpact != null) {
            val upcoming = nextHighImpact!!
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BearishRed.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .clickable { selectedEventForDetail = upcoming }
                    .testTag("next_high_impact_banner"),
                color = BearishRed.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BearishRed,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = TerminalDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "NEXT TIER-1 CATALYST",
                                color = BearishRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "• ${upcoming.getTimeRelativeLabel(nowMs)}",
                                color = TextPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${upcoming.countryFlag} ${upcoming.country}: ${upcoming.title}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = SupportedTimeZones.formatEventTime(upcoming.utcTimestamp, timeZone),
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter releases (e.g. CPI, NFP, Powell, PMI)…", fontSize = 11.sp, color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeutralBlue,
                unfocusedBorderColor = TerminalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = TerminalSurfaceCard,
                unfocusedContainerColor = TerminalSurfaceCard
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .height(44.dp)
                .testTag("calendar_search_field")
        )

        // Day Filters: All Week, Today, Tomorrow, Upcoming
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val days = listOf("ALL" to "All Week", "TODAY" to "Today", "TOMORROW" to "Tomorrow", "UPCOMING" to "Upcoming")
            items(days) { (key, label) ->
                val isSelected = dayFilter == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) NeutralBlue else TerminalSurfaceElevated)
                        .border(0.5.dp, if (isSelected) NeutralBlue else TerminalBorder, RoundedCornerShape(14.dp))
                        .clickable { dayFilter = key }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) TerminalDark else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Impact Filter Pills: All, High Impact Only, Medium, Low
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterPill(
                    label = "All Impact",
                    isSelected = impactFilter == null,
                    onClick = { impactFilter = null },
                    testTag = "filter_impact_all"
                )
            }
            item {
                FilterPill(
                    label = "🔴 High Impact",
                    isSelected = impactFilter == NewsImpact.HIGH,
                    onClick = { impactFilter = NewsImpact.HIGH },
                    testTag = "filter_impact_high",
                    activeColor = BearishRed
                )
            }
            item {
                FilterPill(
                    label = "🟠 Medium",
                    isSelected = impactFilter == NewsImpact.MEDIUM,
                    onClick = { impactFilter = NewsImpact.MEDIUM },
                    testTag = "filter_impact_medium",
                    activeColor = WarningAmber
                )
            }
            item {
                FilterPill(
                    label = "🟡 Low",
                    isSelected = impactFilter == NewsImpact.LOW,
                    onClick = { impactFilter = NewsImpact.LOW },
                    testTag = "filter_impact_low",
                    activeColor = GoldAccent
                )
            }
        }

        // Currency Selector Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(currencies) { curr ->
                val isSelected = currencyFilter == curr
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NeonCyan else TerminalSurfaceElevated)
                        .border(0.5.dp, if (isSelected) NeonCyan else TerminalBorder, RoundedCornerShape(12.dp))
                        .clickable { currencyFilter = curr }
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                        .testTag("filter_curr_$curr")
                ) {
                    Text(
                        text = curr,
                        color = if (isSelected) TerminalDark else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Economic Events Feed
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 85.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredEvents.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp),
                        color = TerminalSurfaceCard,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EventBusy, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No Economic Events Found", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Try clearing filters or search query.", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(filteredEvents, key = { it.id }) { event ->
                    EconomicEventCard(
                        event = event,
                        timeZone = timeZone,
                        nowMs = nowMs,
                        onSelectPair = onSelectPair,
                        onOpenDetail = { selectedEventForDetail = event }
                    )
                }
            }
        }
    }

    // Event Detail Modal Dialog
    if (selectedEventForDetail != null) {
        val event = selectedEventForDetail!!
        EventDetailDialog(
            event = event,
            activeTimeZone = timeZone,
            nowMs = nowMs,
            onSelectPair = {
                selectedEventForDetail = null
                onSelectPair(it)
            },
            onDismiss = { selectedEventForDetail = null }
        )
    }
}

@Composable
private fun EconomicEventCard(
    event: EconomicEvent,
    timeZone: TimeZoneSetting,
    nowMs: Long,
    onSelectPair: (String) -> Unit,
    onOpenDetail: () -> Unit
) {
    val isUpcoming = event.utcTimestamp > nowMs
    val formattedTime = SupportedTimeZones.formatEventTime(event.utcTimestamp, timeZone)
    val relativeLabel = event.getTimeRelativeLabel(nowMs)

    val impactColor = when (event.impact) {
        NewsImpact.HIGH -> BearishRed
        NewsImpact.MEDIUM -> WarningAmber
        NewsImpact.LOW -> Color(0xFF64748B)
        NewsImpact.NON_ECONOMIC -> TextMuted
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (event.impact == NewsImpact.HIGH) impactColor.copy(alpha = 0.45f) else TerminalBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onOpenDetail)
            .testTag("event_card_${event.id}"),
        color = TerminalSurfaceCard,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Flag, Currency, Time, Relative Label, Impact Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(event.countryFlag, fontSize = 16.sp)
                    Text(
                        text = event.country,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(TerminalSurfaceElevated, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = relativeLabel,
                        color = if (isUpcoming) NeonCyan else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Impact Pill
                Box(
                    modifier = Modifier
                        .background(impactColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(0.5.dp, impactColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = event.impact.label.uppercase(),
                        color = impactColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Event Title
            Text(
                text = event.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Metrics: Actual, Forecast, Previous
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Actual", color = TextMuted, fontSize = 9.sp)
                    Text(
                        text = event.actual ?: if (isUpcoming) "Pending" else "--",
                        color = if (event.actual != null) BullishGreen else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Forecast", color = TextMuted, fontSize = 9.sp)
                    Text(
                        text = event.forecast ?: "--",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Previous", color = TextMuted, fontSize = 9.sp)
                    Text(
                        text = event.previous ?: "--",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // SCT Theory Strategic Note
            if (event.sctImpactNote.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .border(0.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(6.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "SCT Impact",
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = event.sctImpactNote,
                        color = Color(0xFFDDD6FE),
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }

            // Affected SCT Pairs Chips
            if (event.affectedSctPairs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Setups:", color = TextMuted, fontSize = 9.sp)
                    event.affectedSctPairs.take(4).forEach { pair ->
                        Box(
                            modifier = Modifier
                                .background(TerminalSurfaceElevated, RoundedCornerShape(4.dp))
                                .border(0.5.dp, NeutralBlue.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .clickable { onSelectPair(pair) }
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = pair,
                                color = NeutralBlue,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDetailDialog(
    event: EconomicEvent,
    activeTimeZone: TimeZoneSetting,
    nowMs: Long,
    onSelectPair: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
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
                        Text(event.countryFlag, fontSize = 20.sp)
                        Column {
                            Text(event.country, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Forex Factory Release", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Multi-session Timezone Clocks
                Text("GLOBAL SESSION RELEASE CLOCKS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalDark, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SupportedTimeZones.ALL_TIME_ZONES.forEach { tz ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(tz.displayName, fontSize = 11.sp, color = if (tz.id == activeTimeZone.id) NeonCyan else TextSecondary)
                            Text(
                                SupportedTimeZones.formatEventTime(event.utcTimestamp, tz),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tz.id == activeTimeZone.id) NeonCyan else TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SCT Institutional Rules
                Text("SCT INSTITUTIONAL EXECUTION PLAYBOOK", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = event.sctImpactNote,
                        color = Color(0xFFDDD6FE),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                if (event.affectedSctPairs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("OPEN SETUP CHARTS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        event.affectedSctPairs.forEach { pair ->
                            Button(
                                onClick = { onSelectPair(pair) },
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeutralBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(pair, color = NeutralBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NeutralBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = TerminalDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    activeColor: Color = NeutralBlue
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) activeColor else TerminalSurfaceElevated,
                RoundedCornerShape(20.dp)
            )
            .border(
                1.dp,
                if (isSelected) activeColor else TerminalBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFF090D16) else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
