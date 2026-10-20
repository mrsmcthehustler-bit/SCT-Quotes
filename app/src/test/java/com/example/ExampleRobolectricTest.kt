package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AssetClass
import com.example.data.model.EnumSctMode
import com.example.data.model.SctSettings
import com.example.data.repository.TradingSignalEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SCT Quotes", appName)
    }

    @Test
    fun `test RSI indicator calculation`() {
        val closes = listOf(
            44.0, 44.25, 44.5, 43.75, 44.0, 44.5, 45.0, 45.25, 45.5, 46.0,
            45.75, 46.5, 46.75, 47.0, 47.25, 47.5
        )
        val rsi = TradingSignalEngine.calculateRsi(closes)
        assertTrue("RSI should be between 0 and 100", rsi in 0.0..100.0)
        assertTrue("Steady rising prices should yield RSI > 60", rsi > 60.0)
    }

    @Test
    fun `test SCT Theory Euro Mode evaluation`() {
        val candles = TradingSignalEngine.generateSyntheticCandles(currentPrice = 1.0850, count = 35)
        val settings = SctSettings(
            operationalMode = EnumSctMode.MODE_EURO,
            fastEmaPeriod = 9,
            slowEmaPeriod = 21,
            riskRewardRatio = 1.5
        )

        val signal = TradingSignalEngine.evaluateSctSignal(
            symbol = "EURUSD",
            name = "Euro / US Dollar",
            assetClass = AssetClass.FOREX,
            currentPrice = 1.0850,
            spread = 0.00012,
            candles = candles,
            settings = settings
        )

        assertNotNull(signal)
        assertEquals("EURUSD", signal.symbol)
        assertEquals("M5", signal.timeframe)
        assertNotNull(signal.sctLevels)
        assertNotNull(signal.sctDashboardStats)
        assertTrue("Risk-to-reward ratio should equal configured ratio", signal.riskRewardRatio == 1.5)
        assertTrue("Asian range high should be set", (signal.sctLevels?.asianHigh ?: 0.0) > 0.0)
        assertTrue("Previous day high should be set", (signal.sctLevels?.pdh ?: 0.0) > 0.0)
    }

    @Test
    fun `test SCT Theory USD Mode evaluation with customizable settings`() {
        val candles = TradingSignalEngine.generateSyntheticCandles(currentPrice = 1.3020, count = 35)
        val settings = SctSettings(
            operationalMode = EnumSctMode.MODE_USD,
            fastEmaPeriod = 12,
            slowEmaPeriod = 26,
            riskRewardRatio = 2.0,
            fiboRetracementLevel = 0.618
        )

        val signal = TradingSignalEngine.evaluateSctSignal(
            symbol = "GBPUSD",
            name = "British Pound / USD",
            assetClass = AssetClass.FOREX,
            currentPrice = 1.3020,
            spread = 0.00018,
            candles = candles,
            settings = settings
        )

        assertNotNull(signal)
        assertEquals("GBPUSD", signal.symbol)
        assertTrue("Custom R:R ratio should be applied", signal.riskRewardRatio == 2.0)
        assertTrue("Dashboard status should be active", signal.sctDashboardStats?.zoneStatus?.contains("USD MODE") == true)
    }

    @Test
    fun `test Economic Calendar schedule and high-impact event filter`() {
        val now = System.currentTimeMillis()
        val events = com.example.data.repository.EconomicCalendarRepository.getCachedEventsOrFallback(now)
        assertTrue("Economic events should not be empty", events.isNotEmpty())

        val highImpactEvents = events.filter { it.impact == com.example.data.model.NewsImpact.HIGH }
        assertTrue("Should have high-impact institutional events", highImpactEvents.isNotEmpty())

        val nextHighImpact = com.example.data.repository.EconomicCalendarRepository.getNextHighImpactEvent(events, now)
        assertNotNull("Should find upcoming high-impact event", nextHighImpact)
        assertTrue("Upcoming event must have future timestamp", (nextHighImpact?.utcTimestamp ?: 0L) > now)
    }

    @Test
    fun `test Supported Time Zones and Harare CAT offset conversion`() {
        val harare = com.example.data.model.SupportedTimeZones.HARARE_CAT
        assertEquals("CAT", harare.id)
        assertEquals(2, harare.offsetHours)
        assertEquals("Africa/Harare", harare.timeZoneId)

        val utc = com.example.data.model.SupportedTimeZones.UTC
        assertEquals("UTC", utc.id)
        assertEquals(0, utc.offsetHours)

        val asianWindowUtc = com.example.data.model.SupportedTimeZones.formatAsianRangeWindow(utc)
        val asianWindowCat = com.example.data.model.SupportedTimeZones.formatAsianRangeWindow(harare)
        assertTrue("UTC Asian window should be 00:00 - 06:00 GMT", asianWindowUtc.contains("00:00 - 06:00"))
        assertTrue("Harare CAT Asian window should be 02:00 - 08:00 CAT", asianWindowCat.contains("02:00 - 08:00"))
    }

    @Test
    fun `test Ninja USDJPY Specialized Logic when enabled`() {
        val candles = TradingSignalEngine.generateSyntheticCandles(currentPrice = 154.500, count = 35)
        val ninjaSettings = com.example.data.model.NinjaSettings(
            enabled = true,
            tradeZoneBreakouts = true,
            oneTradePerDay = true,
            enableTPHunter = true
        )
        val sctSettings = SctSettings(
            operationalMode = EnumSctMode.MODE_USD,
            ninjaSettings = ninjaSettings
        )

        val signal = TradingSignalEngine.evaluateSctSignal(
            symbol = "USDJPY",
            name = "US Dollar / Japanese Yen",
            assetClass = AssetClass.FOREX,
            currentPrice = 154.500,
            spread = 0.015,
            candles = candles,
            settings = sctSettings
        )

        assertNotNull(signal)
        assertEquals("USDJPY", signal.symbol)
        assertEquals(com.example.data.model.SctStrategyArchetype.NINJA_USDJPY, signal.sctArchetype)
        assertTrue("Rationale should reference Ninja USDJPY Engine", signal.rationale.contains("Ninja USDJPY Engine"))
        assertTrue("Execution steps should include SC Zone mapping", signal.executionSteps.any { it.title.contains("SC Zone") })
    }

    @Test
    fun `test DXtrade Order Request creation and payload`() {
        val orderReq = com.example.data.model.DxTradeOrderRequest(
            account = "DX-DEMO-99182",
            symbol = "USDJPY",
            side = "BUY",
            orderType = "MARKET",
            quantity = 0.50,
            price = 154.500,
            stopLoss = 154.200,
            takeProfit = 155.100,
            comment = "SCT Ninja Signal"
        )
        assertEquals("USDJPY", orderReq.symbol)
        assertEquals("BUY", orderReq.side)
        assertEquals(0.50, orderReq.quantity, 0.001)
        assertEquals(154.200, orderReq.stopLoss ?: 0.0, 0.001)
        assertEquals(155.100, orderReq.takeProfit ?: 0.0, 0.001)
    }

    @Test
    fun `test DXtrade Repository order execution`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = com.example.data.repository.DxTradeRepository(context)
        repo.connectDemo()

        val acct = repo.activeAccount.value
        assertNotNull("DXtrade Demo Account should be active", acct)
        assertEquals("ACTIVE (SCT VERIFIED)", acct?.status)

        kotlinx.coroutines.runBlocking {
            val orderReq = com.example.data.model.DxTradeOrderRequest(
                account = acct!!.account,
                symbol = "USDJPY",
                side = "BUY",
                orderType = "MARKET",
                quantity = 0.50,
                price = 149.500,
                stopLoss = 149.000,
                takeProfit = 150.500,
                comment = "SCT Ninja Signal"
            )
            val result = repo.submitOrder(orderReq)
            assertTrue("Order should be filled successfully", result.isSuccess)
            val orderResp = result.getOrNull()
            assertNotNull(orderResp)
            assertEquals("FILLED", orderResp?.status)
            assertEquals("USDJPY", orderResp?.symbol)
            assertEquals("BUY", orderResp?.side)
        }
    }

    @Test
    fun `test Forex Factory Event DTO mapping`() {
        val dto = com.example.data.model.ForexFactoryEventDto(
            title = "Non-Farm Employment Change",
            country = "USD",
            date = "2026-10-06T08:30:00-04:00",
            impact = "High",
            forecast = "170K",
            previous = "142K"
        )
        assertEquals("Non-Farm Employment Change", dto.title)
        assertEquals("USD", dto.country)
        assertEquals("High", dto.impact)
    }
}

