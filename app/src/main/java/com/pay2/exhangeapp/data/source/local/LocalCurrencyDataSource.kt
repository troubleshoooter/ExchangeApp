package com.pay2.exhangeapp.data.source.local

import com.pay2.exhangeapp.data.source.local.dao.CurrencyDao
import com.pay2.exhangeapp.data.source.local.dao.ExchangeRatesDao
import com.pay2.exhangeapp.data.source.local.entity.Currency
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRates
import javax.inject.Inject

class LocalCurrencyDataSource @Inject constructor(
    private val currencyDao: CurrencyDao,
    private val exchangeRatesDao: ExchangeRatesDao
) {
    suspend fun saveCurrencies(currencies: List<Currency>) {
        currencyDao.insert(currencies)
    }

    suspend fun saveExchangeRates(exchangeRates: List<ExchangeRates>) {
        exchangeRatesDao.insert(exchangeRates)
    }

    suspend fun getCurrencies(): List<Currency> {
        return currencyDao.getCurrencies()
    }

    suspend fun getExchangeRates(): List<ExchangeRates> {
        return exchangeRatesDao.getExchangeRates()
    }
}