package com.example

import com.example.data.model.AssetClass
import com.example.data.repository.SctPerformanceEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun sctPerformanceEngine_computesEuroModeForEurUsd() {
        val performance = SctPerformanceEngine.computeFiveDayPerformance(
            symbol = "EURUSD",
            name = "Euro / US Dollar",
            assetClass = AssetClass.FOREX,
            currentPrice = 1.0845
        )

        assertEquals("EURUSD", performance.symbol)
        assertTrue(performance.mode.contains("EURO MODE"))
        assertEquals(5, performance.dailySetups.size)
        assertEquals(0.0001, performance.pipSize, 0.00001)
        assertTrue(performance.winRatePercent in 0.0..100.0)
        assertEquals(5, performance.totalWins + performance.totalLosses)
    }

    @Test
    fun sctPerformanceEngine_computesUsdModeForGbpUsdAndUsdJpy() {
        val gbpPerformance = SctPerformanceEngine.computeFiveDayPerformance(
            symbol = "GBPUSD",
            name = "British Pound / USD",
            assetClass = AssetClass.FOREX,
            currentPrice = 1.3020
        )
        assertTrue(gbpPerformance.mode.contains("EMA 9/21"))
        assertEquals(5, gbpPerformance.dailySetups.size)

        val jpyPerformance = SctPerformanceEngine.computeFiveDayPerformance(
            symbol = "USDJPY",
            name = "US Dollar / Japanese Yen",
            assetClass = AssetClass.FOREX,
            currentPrice = 149.25
        )
        assertTrue(jpyPerformance.mode.contains("0.618 Fibonacci"))
        assertEquals(0.01, jpyPerformance.pipSize, 0.001)
        assertEquals(5, jpyPerformance.dailySetups.size)
    }

    @Test
    fun sctPerformanceEngine_computesCryptoAndMetalsSetups() {
        val btcPerformance = SctPerformanceEngine.computeFiveDayPerformance(
            symbol = "BTCUSD",
            name = "Bitcoin / USD",
            assetClass = AssetClass.CRYPTO,
            currentPrice = 64280.0
        )
        assertEquals(5, btcPerformance.dailySetups.size)
        assertTrue(btcPerformance.dailySetups.all { it.entryPrice > 0.0 })
        assertTrue(btcPerformance.dailySetups.all { it.takeProfit > 0.0 })
        assertTrue(btcPerformance.dailySetups.all { it.stopLoss > 0.0 })
    }
}
