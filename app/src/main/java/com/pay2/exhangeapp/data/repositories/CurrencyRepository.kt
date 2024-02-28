package com.pay2.exhangeapp.data.repositories

import com.pay2.exhangeapp.data.source.local.entity.Currency
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRates

interface CurrencyRepository {
    suspend fun getCurrencies(): List<Currency>
    suspend fun getExchangeRates(sourceCurrency: String): List<ExchangeRates>
}