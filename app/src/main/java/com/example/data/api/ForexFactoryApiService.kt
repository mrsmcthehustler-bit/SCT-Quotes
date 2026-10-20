package com.example.data.api

import retrofit2.http.GET

data class ForexFactoryEventDto(
    val title: String? = null,
    val country: String? = null,
    val date: String? = null,
    val impact: String? = null,
    val forecast: String? = null,
    val previous: String? = null,
    val actual: String? = null
)

interface ForexFactoryApiService {
    @GET("ff_calendar_thisweek.json")
    suspend fun getThisWeekCalendar(): List<ForexFactoryEventDto>
}
