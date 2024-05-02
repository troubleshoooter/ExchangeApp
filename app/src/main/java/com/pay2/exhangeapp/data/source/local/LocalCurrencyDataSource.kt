package com.pay2.exhangeapp.data.source.local

import com.pay2.exhangeapp.data.source.local.dao.CurrencyDao
import com.pay2.exhangeapp.data.source.local.dao.ExchangeRatesDao
import com.pay2.exhangeapp.data.source.local.entity.CurrencyEntity
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRatesEntity
import javax.inject.Inject

class LocalCurrencyDataSource @Inject constructor(
    private val currencyDao: CurrencyDao,
    private val exchangeRatesDao: ExchangeRatesDao
) {
    suspend fun saveCurrencies(currencies: List<CurrencyEntity>) {
        currencyDao.insert(currencies)
    }

    suspend fun saveExchangeRates(exchangeRates: List<ExchangeRatesEntity>) {
        exchangeRatesDao.insert(exchangeRates)
    }

    suspend fun getCurrencies(): List<CurrencyEntity> {
        return currencyDao.getCurrencies()
    }

    suspend fun getExchangeRates(): List<ExchangeRatesEntity> {
        return exchangeRatesDao.getExchangeRates()
    }
}