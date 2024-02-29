package com.pay2.exhangeapp.data.source.remote

import com.pay2.exhangeapp.data.source.local.entity.CurrencyEntity
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRatesEntity
import com.pay2.exhangeapp.data.source.remote.api.ApiService
import javax.inject.Inject

class RemoteCurrencyDataSource @Inject constructor(private val apiService: ApiService) {
    suspend fun getCurrencies(): List<CurrencyEntity> {
        val result = apiService.getCurrencies()
        return parseCurrencies(result)
    }

    suspend fun getExchangeRates(baseCurrency: String): List<ExchangeRatesEntity> {
        // commenting as i can't use premium plan and free only provides for USD base
//        val result = apiService.getExchangeRates(baseCurrency)
        val result = apiService.getExchangeRates("USD")
        return parseExchangeRates(result.rates)
    }

    private fun parseCurrencies(currencyMap: Map<String, String>): List<CurrencyEntity> {
        return currencyMap.map {
            CurrencyEntity(code = it.key, name = it.value)
        }
    }

    private fun parseExchangeRates(currencyMap: Map<String, Double>): List<ExchangeRatesEntity> {
        return currencyMap.map {
            ExchangeRatesEntity(code = it.key, rate = it.value)
        }
    }
}