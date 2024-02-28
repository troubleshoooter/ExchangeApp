package com.pay2.exhangeapp.data.source.remote

import com.pay2.exhangeapp.data.source.local.entity.Currency
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRates
import com.pay2.exhangeapp.data.source.remote.api.ApiService
import javax.inject.Inject

class RemoteCurrencyDataSource @Inject constructor(private val apiService: ApiService) {
    suspend fun getCurrencies(): List<Currency> {
        val result = apiService.getCurrencies()
        return parseCurrencies(result)
    }

    suspend fun getExchangeRates(baseCurrency: String): List<ExchangeRates> {
        // commenting as i can't use premium plan and free only provides for USD base
//        val result = apiService.getExchangeRates(baseCurrency)
        val result = apiService.getExchangeRates("USD")
        return parseExchangeRates(result.rates)
    }

    private fun parseCurrencies(currencyMap: Map<String, String>): List<Currency> {
        return currencyMap.map {
            Currency(it.key, it.value)
        }
    }

    private fun parseExchangeRates(currencyMap: Map<String, Double>): List<ExchangeRates> {
        return currencyMap.map {
            ExchangeRates(it.key, it.value)
        }
    }
}