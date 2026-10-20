package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OhlcCandle
import com.example.data.model.SctLevels
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import kotlin.math.abs
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeutralBlue
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurfaceCard
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import java.text.DecimalFormat

@Composable
fun CandlestickChart(
    candles: List<OhlcCandle>,
    entryPrice: Double? = null,
    tp1: Double? = null,
    tp2: Double? = null,
    stopLoss: Double? = null,
    currentPrice: Double? = null,
    isBullish: Boolean = true,
    sctLevels: SctLevels? = null,
    showWatermark: Boolean = false,
    watermarkMain: String = "",
    watermarkSub: String = "",
    modifier: Modifier = Modifier
) {
    val formatter = remember { DecimalFormat("#,##0.0000") }
    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }
    val displayCandle = selectedCandleIndex?.let { idx ->
        if (idx in candles.indices) candles[idx] else null
    } ?: candles.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TerminalSurfaceCard)
            .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("chart_container")
    ) {
        // Chart Header with O/H/L/C details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SCT M5 Chart",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(NeutralBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, NeutralBlue, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "ASIAN BOX + EMA 9/21",
                            color = NeutralBlue,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = if (selectedCandleIndex != null) "Inspecting selected candle" else "Tap candles to inspect OHLC levels",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            if (displayCandle != null) {
                val isCandleBull = displayCandle.close >= displayCandle.open
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "O: ${formatter.format(displayCandle.open)}",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "H: ${formatter.format(displayCandle.high)}",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "L: ${formatter.format(displayCandle.low)}",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "C: ${formatter.format(displayCandle.close)}",
                        color = if (isCandleBull) BullishGreen else BearishRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Candlestick Canvas with SCT Overlays
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candles) {
                        detectTapGestures(
                            onPress = { offset ->
                                if (candles.isNotEmpty()) {
                                    val candleWidth = size.width / candles.size
                                    val idx = (offset.x / candleWidth).toInt().coerceIn(0, candles.lastIndex)
                                    selectedCandleIndex = idx
                                }
                            },
                            onTap = { selectedCandleIndex = null }
                        )
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val candleCount = candles.size
                if (candleCount < 2) return@Canvas

                val minPriceCandles = candles.minOf { it.low }
                val maxPriceCandles = candles.maxOf { it.high }

                val allPrices = mutableListOf(minPriceCandles, maxPriceCandles).apply {
                    entryPrice?.let { add(it) }
                    tp1?.let { add(it) }
                    tp2?.let { add(it) }
                    stopLoss?.let { add(it) }
                    sctLevels?.asianHigh?.let { if (it > 0) add(it) }
                    sctLevels?.asianLow?.let { if (it > 0) add(it) }
                    sctLevels?.pdh?.let { if (it > 0) add(it) }
                    sctLevels?.pdl?.let { if (it > 0) add(it) }
                    sctLevels?.fibEntryLevel?.let { if (it > 0) add(it) }
                }

                val minPrice = (allPrices.minOrNull() ?: minPriceCandles) * 0.9992
                val maxPrice = (allPrices.maxOrNull() ?: maxPriceCandles) * 1.0008
                val priceRange = (maxPrice - minPrice).coerceAtLeast(0.00001)

                fun priceToY(price: Double): Float {
                    val normalized = (price - minPrice) / priceRange
                    return (canvasHeight - (normalized * canvasHeight)).toFloat()
                }

                val candleSlotWidth = canvasWidth / candleCount
                val candleBarWidth = (candleSlotWidth * 0.65f).coerceAtLeast(2f)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                // 1. SCT ASIAN RANGE SHADED BOX (00:00 - 06:00 GMT)
                if (sctLevels != null && sctLevels.asianHigh > 0 && sctLevels.asianLow > 0) {
                    val asianCandlesCount = minOf(sctLevels.asianCandleCount, candleCount)
                    val asianEndX = asianCandlesCount * candleSlotWidth
                    val asianYTop = priceToY(sctLevels.asianHigh)
                    val asianYBottom = priceToY(sctLevels.asianLow)
                    val boxHeight = abs(asianYBottom - asianYTop).coerceAtLeast(4f)
                    val boxTop = minOf(asianYTop, asianYBottom)

                    // Shaded Asian session box
                    drawRect(
                        color = Color(0xFF8B5CF6).copy(alpha = 0.08f),
                        topLeft = Offset(0f, boxTop),
                        size = Size(asianEndX, boxHeight)
                    )

                    // Asian High line across chart
                    drawLine(
                        color = Color(0xFFA78BFA).copy(alpha = 0.75f),
                        start = Offset(0f, asianYTop),
                        end = Offset(canvasWidth, asianYTop),
                        strokeWidth = 1.4f,
                        pathEffect = dashEffect
                    )

                    // Asian Low line across chart
                    drawLine(
                        color = Color(0xFFA78BFA).copy(alpha = 0.75f),
                        start = Offset(0f, asianYBottom),
                        end = Offset(canvasWidth, asianYBottom),
                        strokeWidth = 1.4f,
                        pathEffect = dashEffect
                    )
                }

                // 2. PREVIOUS DAY HIGH (PDH) & LOW (PDL) - INSTITUTIONAL LIQUIDITY POOLS
                sctLevels?.pdh?.let { pdh ->
                    if (pdh > 0) {
                        val y = priceToY(pdh)
                        drawLine(
                            color = Color(0xFFF43F5E).copy(alpha = 0.8f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }
                }
                sctLevels?.pdl?.let { pdl ->
                    if (pdl > 0) {
                        val y = priceToY(pdl)
                        drawLine(
                            color = Color(0xFFF43F5E).copy(alpha = 0.8f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }
                }

                // 3. FIBONACCI 61.8% GOLDEN POCKET LEVEL (USD Mode)
                sctLevels?.fibEntryLevel?.let { fib ->
                    if (fib > 0) {
                        val y = priceToY(fib)
                        drawLine(
                            color = GoldAccent.copy(alpha = 0.9f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.5f,
                            pathEffect = dashEffect
                        )
                    }
                }

                // 4. STOP LOSS TARGET LINE
                stopLoss?.let { sl ->
                    drawLine(
                        color = BearishRed.copy(alpha = 0.85f),
                        start = Offset(0f, priceToY(sl)),
                        end = Offset(canvasWidth, priceToY(sl)),
                        strokeWidth = 1.8f,
                        pathEffect = dashEffect
                    )
                }

                // 5. ENTRY ORDER TARGET LINE
                entryPrice?.let { entry ->
                    drawLine(
                        color = NeutralBlue.copy(alpha = 0.9f),
                        start = Offset(0f, priceToY(entry)),
                        end = Offset(canvasWidth, priceToY(entry)),
                        strokeWidth = 1.8f,
                        pathEffect = dashEffect
                    )
                }

                // 6. TAKE PROFIT TARGET LINE
                tp1?.let { t1 ->
                    drawLine(
                        color = BullishGreen.copy(alpha = 0.9f),
                        start = Offset(0f, priceToY(t1)),
                        end = Offset(canvasWidth, priceToY(t1)),
                        strokeWidth = 1.8f,
                        pathEffect = dashEffect
                    )
                }

                // 7. DRAW CANDLESTICKS
                candles.forEachIndexed { index, candle ->
                    val isBull = candle.close >= candle.open
                    val color = if (isBull) BullishGreen else BearishRed
                    val centerX = index * candleSlotWidth + (candleSlotWidth / 2f)

                    val openY = priceToY(candle.open)
                    val closeY = priceToY(candle.close)
                    val highY = priceToY(candle.high)
                    val lowY = priceToY(candle.low)

                    // Wick
                    drawLine(
                        color = color,
                        start = Offset(centerX, highY),
                        end = Offset(centerX, lowY),
                        strokeWidth = 1.5f
                    )

                    // Body
                    val bodyTop = minOf(openY, closeY)
                    val bodyBottom = maxOf(openY, closeY)
                    drawRect(
                        color = color,
                        topLeft = Offset(centerX - (candleBarWidth / 2f), bodyTop),
                        size = Size(candleBarWidth, (bodyBottom - bodyTop).coerceAtLeast(2f))
                    )
                }

                // 8. CONTINUOUS FAST EMA 9 CURVE (Vibrant Blue)
                val ema9List = sctLevels?.ema9Series ?: emptyList()
                if (ema9List.size == candleCount && candleCount > 1) {
                    val pathEma9 = Path()
                    ema9List.forEachIndexed { index, emaVal ->
                        val x = index * candleSlotWidth + (candleSlotWidth / 2f)
                        val y = priceToY(emaVal)
                        if (index == 0) pathEma9.moveTo(x, y) else pathEma9.lineTo(x, y)
                    }
                    drawPath(
                        path = pathEma9,
                        color = NeutralBlue,
                        style = Stroke(width = 2.0f, cap = StrokeCap.Round)
                    )
                }

                // 9. CONTINUOUS SLOW EMA 21 CURVE (Amber Ribbon)
                val ema21List = sctLevels?.ema21Series ?: emptyList()
                if (ema21List.size == candleCount && candleCount > 1) {
                    val pathEma21 = Path()
                    ema21List.forEachIndexed { index, emaVal ->
                        val x = index * candleSlotWidth + (candleSlotWidth / 2f)
                        val y = priceToY(emaVal)
                        if (index == 0) pathEma21.moveTo(x, y) else pathEma21.lineTo(x, y)
                    }
                    drawPath(
                        path = pathEma21,
                        color = WarningAmber,
                        style = Stroke(width = 2.0f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SCT Technical Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurfaceElevated, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LegendIndicator(color = NeutralBlue, label = "EMA 9")
                LegendIndicator(color = WarningAmber, label = "EMA 21")
                LegendIndicator(color = Color(0xFFA78BFA), label = "Asian Box")
                LegendIndicator(color = Color(0xFFF43F5E), label = "PDH/PDL")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                entryPrice?.let {
                    Text("Entry: ${formatter.format(it)}", color = NeutralBlue, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                tp1?.let {
                    Text("TP: ${formatter.format(it)}", color = BullishGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun LegendIndicator(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
