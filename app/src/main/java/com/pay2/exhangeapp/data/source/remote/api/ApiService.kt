package com.pay2.exhangeapp.data.source.remote.api

import com.pay2.exhangeapp.data.source.remote.models.ExchangeRatesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("currencies.json")
    suspend fun getCurrencies(): Map<String, String>

    @GET("latest.json")
    suspend fun getExchangeRates(
        @Query("base") base: String
    ): ExchangeRatesResponse
}