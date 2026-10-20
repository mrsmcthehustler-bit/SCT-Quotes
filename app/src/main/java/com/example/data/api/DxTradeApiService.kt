package com.example.data.api

import com.example.data.model.DxTradeAccount
import com.example.data.model.DxTradeLoginRequest
import com.example.data.model.DxTradeLoginResponse
import com.example.data.model.DxTradeOrderRequest
import com.example.data.model.DxTradeOrderResponse
import com.example.data.model.DxTradePosition
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface DxTradeApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: DxTradeLoginRequest
    ): Response<DxTradeLoginResponse>

    @POST("api/auth/ping")
    suspend fun ping(
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("api/accounts")
    suspend fun getAccounts(
        @Header("Authorization") token: String
    ): Response<List<DxTradeAccount>>

    @GET("api/accounts/{account}/summary")
    suspend fun getAccountSummary(
        @Path("account") account: String,
        @Header("Authorization") token: String
    ): Response<DxTradeAccount>

    @GET("api/accounts/{account}/positions")
    suspend fun getPositions(
        @Path("account") account: String,
        @Header("Authorization") token: String
    ): Response<List<DxTradePosition>>

    @POST("api/accounts/{account}/orders")
    suspend fun createOrder(
        @Path("account") account: String,
        @Header("Authorization") token: String,
        @Body order: DxTradeOrderRequest
    ): Response<DxTradeOrderResponse>

    @POST("api/accounts/{account}/positions/{positionId}/close")
    suspend fun closePosition(
        @Path("account") account: String,
        @Path("positionId") positionId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>
}
