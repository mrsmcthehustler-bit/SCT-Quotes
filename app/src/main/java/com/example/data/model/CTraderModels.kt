package com.example.data.model

import com.example.BuildConfig

data class CTraderAccount(
    val accountId: String,
    val accountNumber: String,
    val brokerName: String,
    val currency: String = "USD",
    val balance: Double = 50000.0,
    val equity: Double = 50000.0,
    val margin: Double = 0.0,
    val freeMargin: Double = 50000.0,
    val leverage: Int = 500,
    val isLive: Boolean = false
)

data class CTraderPosition(
    val positionId: Long,
    val symbol: String,
    val tradeSide: String, // "BUY" or "SELL"
    val volumeLots: Double,
    val entryPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val netProfit: Double = 0.0,
    val sctStrategyOrigin: String = "SCT THEORY",
    val entryTimeMs: Long = System.currentTimeMillis()
) {
    val isBullish: Boolean get() = tradeSide == "BUY"
}

data class CTraderOrderRequest(
    val symbol: String,
    val isBuy: Boolean,
    val volumeLots: Double,
    val entryPrice: Double,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val orderType: String = "MARKET",
    val sctStrategyOrigin: String = "SCT THEORY",
    val comment: String = "SCT Quotes Trade"
)

data class CTraderOrderResult(
    val isSuccess: Boolean,
    val orderId: Long? = null,
    val message: String,
    val position: CTraderPosition? = null
)

data class CTraderAuthState(
    val isAuthorized: Boolean = false,
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUri: String = CTraderAppRegistrationDetails.DEFAULT_REDIRECT_URI,
    val hasClientCredentials: Boolean = false,
    val isDemoMode: Boolean = true,
    val activeAccount: CTraderAccount? = null,
    val availableAccounts: List<CTraderAccount> = emptyList(),
    val openPositions: List<CTraderPosition> = emptyList(),
    val lastSyncTimeMs: Long = 0L,
    val statusMessage: String = "Disconnected from cTrader",
    val accessToken: String? = null
)

object CTraderAppRegistrationDetails {
    const val APP_NAME = "SCT Quotes Institutional Terminal"
    const val PORTAL_URL = "https://openapi.ctrader.com/apps"
    const val DOCS_URL = "https://openapi.ctrader.com/docs"
    const val DEFAULT_REDIRECT_URI = "sctquotes://oauth"
    const val WEB_REDIRECT_URI = "https://ais-dev-byd3qsa5mj2s3tqmsqy2nb-154422508310.europe-west1.run.app/oauth/ctrader"
    val REQUIRED_SCOPES = listOf("trading", "accounts")
    const val APP_DESCRIPTION = "Institutional SCT Theory execution terminal for Spotware cTrader. Features M5 Asian Range breakout/retest, EMA 9/21 ribbon pockets, and PDH/PDL targets."

    fun getClientId(): String {
        return try {
            BuildConfig.CTRADER_CLIENT_ID.takeIf { it.isNotEmpty() && !it.contains("your_ctrader") } ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    fun getClientSecret(): String {
        return try {
            BuildConfig.CTRADER_CLIENT_SECRET.takeIf { it.isNotEmpty() && !it.contains("your_ctrader") } ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    fun getRedirectUri(): String {
        return try {
            BuildConfig.CTRADER_REDIRECT_URI.takeIf { it.isNotEmpty() } ?: DEFAULT_REDIRECT_URI
        } catch (_: Exception) {
            DEFAULT_REDIRECT_URI
        }
    }

    fun isConfiguredInBuildConfig(): Boolean {
        return getClientId().isNotEmpty() && getClientSecret().isNotEmpty()
    }

    fun buildOAuthUrl(clientId: String = getClientId(), redirectUri: String = getRedirectUri()): String {
        val targetClient = clientId.ifEmpty { "YOUR_CLIENT_ID" }
        val scopesEncoded = REQUIRED_SCOPES.joinToString("%20")
        return "https://connect.spotware.com/apps/auth?client_id=$targetClient&redirect_uri=$redirectUri&scope=$scopesEncoded"
    }
}
