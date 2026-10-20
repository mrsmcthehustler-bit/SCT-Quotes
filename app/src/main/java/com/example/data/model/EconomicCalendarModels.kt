package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

typealias ForexFactoryEventDto = com.example.data.api.ForexFactoryEventDto

enum class NewsImpact(val label: String, val level: Int) {
    HIGH("High Impact", 3),
    MEDIUM("Medium Impact", 2),
    LOW("Low Impact", 1),
    NON_ECONOMIC("Holiday", 0);

    companion object {
        fun fromString(str: String?): NewsImpact {
            return when (str?.trim()?.lowercase(Locale.ROOT)) {
                "high" -> HIGH
                "medium" -> MEDIUM
                "low" -> LOW
                "holiday", "non-economic", "none" -> NON_ECONOMIC
                else -> LOW
            }
        }
    }
}

data class ForexFactoryCalendarStatus(
    val isLive: Boolean = false,
    val lastSyncMs: Long = 0L,
    val totalEvents: Int = 0,
    val isRateLimited: Boolean = false,
    val errorMessage: String? = null,
    val source: String = "Forex Factory Feed (nfs.faireconomy.media)"
)

data class EconomicEvent(
    val id: String,
    val title: String,
    val country: String, // e.g. "USD", "EUR", "GBP", "JPY", "CAD", "CHF", "AUD", "NZD", "CNY"
    val countryFlag: String,
    val utcTimestamp: Long,
    val dateRaw: String = "",
    val impact: NewsImpact,
    val actual: String? = null,
    val forecast: String? = null,
    val previous: String? = null,
    val sctImpactNote: String = "",
    val affectedSctPairs: List<String> = emptyList()
) {
    fun isPast(nowMs: Long = System.currentTimeMillis()): Boolean {
        return utcTimestamp <= nowMs
    }

    fun getTimeRelativeLabel(nowMs: Long = System.currentTimeMillis()): String {
        val diff = utcTimestamp - nowMs
        if (diff > 0) {
            val totalMins = diff / (60 * 1000)
            val hours = totalMins / 60
            val mins = totalMins % 60
            return when {
                hours > 24 -> "In ${hours / 24}d ${hours % 24}h"
                hours > 0 -> "In ${hours}h ${mins}m"
                else -> "In ${mins}m"
            }
        } else {
            val totalMins = (-diff) / (60 * 1000)
            val hours = totalMins / 60
            val mins = totalMins % 60
            return when {
                totalMins < 5 -> "Just Released"
                hours > 24 -> "${hours / 24}d ago"
                hours > 0 -> "${hours}h ago"
                else -> "${mins}m ago"
            }
        }
    }

    fun isToday(timeZone: TimeZoneSetting, nowMs: Long = System.currentTimeMillis()): Boolean {
        val tz = TimeZone.getTimeZone(timeZone.timeZoneId)
        val calEvent = Calendar.getInstance(tz).apply { timeInMillis = utcTimestamp }
        val calNow = Calendar.getInstance(tz).apply { timeInMillis = nowMs }
        return calEvent.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calEvent.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
    }

    fun isTomorrow(timeZone: TimeZoneSetting, nowMs: Long = System.currentTimeMillis()): Boolean {
        val tz = TimeZone.getTimeZone(timeZone.timeZoneId)
        val calEvent = Calendar.getInstance(tz).apply { timeInMillis = utcTimestamp }
        val calTomorrow = Calendar.getInstance(tz).apply {
            timeInMillis = nowMs
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return calEvent.get(Calendar.YEAR) == calTomorrow.get(Calendar.YEAR) &&
                calEvent.get(Calendar.DAY_OF_YEAR) == calTomorrow.get(Calendar.DAY_OF_YEAR)
    }
}

data class TimeZoneSetting(
    val id: String,
    val displayName: String,
    val offsetHours: Int,
    val abbreviation: String,
    val locationName: String,
    val timeZoneId: String
)

object SupportedTimeZones {
    val UTC = TimeZoneSetting(
        id = "UTC",
        displayName = "UTC / GMT (Institutional)",
        offsetHours = 0,
        abbreviation = "GMT",
        locationName = "London Baseline",
        timeZoneId = "UTC"
    )

    val HARARE_CAT = TimeZoneSetting(
        id = "CAT",
        displayName = "Harare / CAT (UTC+2)",
        offsetHours = 2,
        abbreviation = "CAT",
        locationName = "Harare / Johannesburg",
        timeZoneId = "Africa/Harare"
    )

    val NEW_YORK_EST = TimeZoneSetting(
        id = "EST",
        displayName = "New York / EST (UTC-5)",
        offsetHours = -5,
        abbreviation = "EST",
        locationName = "Wall Street",
        timeZoneId = "America/New_York"
    )

    val LONDON_BST = TimeZoneSetting(
        id = "BST",
        displayName = "London / BST (UTC+1)",
        offsetHours = 1,
        abbreviation = "BST",
        locationName = "City of London",
        timeZoneId = "Europe/London"
    )

    val TOKYO_JST = TimeZoneSetting(
        id = "JST",
        displayName = "Tokyo / JST (UTC+9)",
        offsetHours = 9,
        abbreviation = "JST",
        locationName = "Tokyo Asian Session",
        timeZoneId = "Asia/Tokyo"
    )

    val ALL_TIME_ZONES = listOf(
        UTC,
        HARARE_CAT,
        NEW_YORK_EST,
        LONDON_BST,
        TOKYO_JST
    )

    fun formatEventTime(utcTimestamp: Long, timeZone: TimeZoneSetting): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US).apply {
            this.timeZone = TimeZone.getTimeZone(timeZone.timeZoneId)
        }
        return "${sdf.format(Date(utcTimestamp))} ${timeZone.abbreviation}"
    }

    fun formatEventDate(utcTimestamp: Long, timeZone: TimeZoneSetting): String {
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.US).apply {
            this.timeZone = TimeZone.getTimeZone(timeZone.timeZoneId)
        }
        return sdf.format(Date(utcTimestamp))
    }

    fun formatCurrentClock(timeZone: TimeZoneSetting): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US).apply {
            this.timeZone = TimeZone.getTimeZone(timeZone.timeZoneId)
        }
        return "${sdf.format(Date())} ${timeZone.abbreviation}"
    }

    fun formatAsianRangeWindow(timeZone: TimeZoneSetting): String {
        val startHour = (0 + timeZone.offsetHours + 24) % 24
        val endHour = (6 + timeZone.offsetHours + 24) % 24
        val startStr = String.format("%02d:00", startHour)
        val endStr = String.format("%02d:00", endHour)
        return "$startStr - $endStr ${timeZone.abbreviation}"
    }

    fun formatLondonOpenWindow(timeZone: TimeZoneSetting): String {
        val startHour = (7 + timeZone.offsetHours + 24) % 24
        val endHour = (11 + timeZone.offsetHours + 24) % 24
        val startStr = String.format("%02d:00", startHour)
        val endStr = String.format("%02d:00", endHour)
        return "$startStr - $endStr ${timeZone.abbreviation}"
    }
}
