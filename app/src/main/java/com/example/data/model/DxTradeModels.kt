package com.example.data.model

data class DxTradeCredentials(
    val serverUrl: String = "https://demo.dx.trade",
    val username: String = "demo_trader",
    val password: String = "SctQuotes2026!",
    val accountId: String = "DEMO-888999",
    val domain: String = "default",
    val isDemoMode: Boolean = true
)

data class DxTradeLoginRequest(
    val username: String,
    val password: String,
    val domain: String = "default"
)

data class DxTradeLoginResponse(
    val sessionToken: String? = null,
    val userId: String? = null,
    val accounts: List<String>? = null,
    val message: String? = null
)

data class DxTradeAccount(
    val account: String,
    val currency: String = "USD",
    val balance: Double = 100000.0,
    val equity: Double = 100000.0,
    val freeMargin: Double = 98500.0,
    val margin: Double = 1500.0,
    val marginLevel: Double = 6666.0,
    val leverage: Int = 100,
    val status: String = "ACTIVE"
)

data class DxTradePosition(
    val positionId: String,
    val account: String,
    val symbol: String,
    val side: String, // "BUY" or "SELL"
    val quantity: Double, // Lot size
    val openPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val pl: Double = 0.0,
    val openTime: Long = System.currentTimeMillis()
)

data class DxTradeOrderRequest(
    val account: String,
    val symbol: String,
    val side: String, // "BUY" or "SELL"
    val orderType: String = "MARKET", // "MARKET" or "LIMIT"
    val quantity: Double = 0.1,
    val price: Double? = null,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val comment: String? = "SCT Quotes Trade"
)

data class DxTradeOrderResponse(
    val orderId: String,
    val account: String,
    val status: String, // "FILLED", "ACCEPTED", "REJECTED"
    val symbol: String,
    val side: String,
    val quantity: Double,
    val executionPrice: Double,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class DxTradeConnectionState(val label: String) {
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting…"),
    CONNECTED("Connected"),
    ERROR("Connection Error")
}
