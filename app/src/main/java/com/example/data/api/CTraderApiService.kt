package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class CTraderTokenResponse(
    val accessToken: String? = null,
    val tokenType: String? = null,
    val expiresIn: Long? = null,
    val refreshToken: String? = null,
    val errorCode: String? = null,
    val errorDescription: String? = null
)

data class CTraderOpenApiAccountDto(
    val accountId: Long? = null,
    val accountNumber: Long? = null,
    val brokerTitle: String? = null,
    val depositCurrency: String? = null,
    val leverage: Int? = null,
    val isLive: Boolean? = null,
    val traderRegistrationTimestamp: Long? = null
)

data class CTraderAccountsListResponse(
    val data: List<CTraderOpenApiAccountDto>? = null,
    val errorCode: String? = null,
    val errorDescription: String? = null
)

interface CTraderConnectService {
    @FormUrlEncoded
    @POST("apps/token")
    suspend fun exchangeToken(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<CTraderTokenResponse>

    @FormUrlEncoded
    @POST("apps/token")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<CTraderTokenResponse>
}

interface CTraderAccountsService {
    @GET("connect/tradingaccounts")
    suspend fun getTradingAccounts(
        @Header("Authorization") bearerToken: String
    ): Response<CTraderAccountsListResponse>
}

object CTraderNetworkClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val connectService: CTraderConnectService by lazy {
        Retrofit.Builder()
            .baseUrl("https://connect.spotware.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CTraderConnectService::class.java)
    }

    val accountsService: CTraderAccountsService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.spotware.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CTraderAccountsService::class.java)
    }
}
