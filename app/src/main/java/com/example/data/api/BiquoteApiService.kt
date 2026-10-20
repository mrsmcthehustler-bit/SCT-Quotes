package com.example.data.api

import com.example.data.model.BiquoteTick
import com.example.data.model.OhlcCandle
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BiquoteApiService {

    @GET("api/{symbol}")
    suspend fun getTick(
        @Path("symbol") symbol: String
    ): Response<BiquoteTick>

    @GET("api/latest")
    suspend fun getLatestTicks(
        @Query("symbols") symbols: List<String>
    ): Response<List<BiquoteTick>>

    @GET("api/{symbol}/ohlc")
    suspend fun getOhlc(
        @Path("symbol") symbol: String,
        @Query("timeframe") timeframe: String? = null
    ): Response<List<OhlcCandle>>

    @GET("api/market/gainers")
    suspend fun getMarketGainers(): Response<List<BiquoteTick>>

    @GET("api/market/losers")
    suspend fun getMarketLosers(): Response<List<BiquoteTick>>

    @GET("api/market/most-active")
    suspend fun getMarketMostActive(): Response<List<BiquoteTick>>

    @GET("api/symbols")
    suspend fun getSymbols(): Response<List<Any>>
}
