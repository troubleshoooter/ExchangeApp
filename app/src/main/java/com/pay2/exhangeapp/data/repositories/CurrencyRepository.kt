package com.pay2.exhangeapp.data.repositories

import com.pay2.exhangeapp.data.models.Currency
import com.pay2.exhangeapp.data.models.ExchangeRates
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    fun getCurrencies(): Flow<Result<List<Currency>>>
    fun getExchangeRates(sourceCurrency: String): Flow<Result<List<ExchangeRates>>>
}