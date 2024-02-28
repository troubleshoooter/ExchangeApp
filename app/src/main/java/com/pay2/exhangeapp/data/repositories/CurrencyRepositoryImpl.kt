package com.pay2.exhangeapp.data.repositories

import com.pay2.exhangeapp.common.NetworkConst
import com.pay2.exhangeapp.common.NetworkUtil
import com.pay2.exhangeapp.data.source.local.LocalCurrencyDataSource
import com.pay2.exhangeapp.data.source.local.dao.RefreshSchedulesDao
import com.pay2.exhangeapp.data.source.local.entity.Currency
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRates
import com.pay2.exhangeapp.data.source.local.entity.RefreshSchedules
import com.pay2.exhangeapp.data.source.remote.RemoteCurrencyDataSource
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class CurrencyRepositoryImpl @Inject constructor(
    private val localCurrencySource: LocalCurrencyDataSource,
    private val remoteCurrencySource: RemoteCurrencyDataSource,
    private val refreshSchedulesDao: RefreshSchedulesDao,
    private val networkUtil: NetworkUtil
) : CurrencyRepository {

    override suspend fun getCurrencies(): List<Currency> {
        return if (shouldFetchFromRemote(NetworkConst.EndPoints.CURRENCIES) && networkUtil.isConnectedToInternet()) {
            val currencies = remoteCurrencySource.getCurrencies()
            localCurrencySource.saveCurrencies(currencies)
            updateRefreshSchedule(NetworkConst.EndPoints.CURRENCIES)
            currencies
        } else {
            localCurrencySource.getCurrencies()
        }
    }

    override suspend fun getExchangeRates(sourceCurrency: String): List<ExchangeRates> {
        return if (shouldFetchFromRemote(NetworkConst.EndPoints.EXCHANGE_RATES)
            && networkUtil.isConnectedToInternet()
        ) {
            val exchangeRates = remoteCurrencySource.getExchangeRates(sourceCurrency)
            localCurrencySource.saveExchangeRates(exchangeRates)
            // future proofing for other currencies
            updateRefreshSchedule(NetworkConst.EndPoints.EXCHANGE_RATES + "/$sourceCurrency")
            exchangeRates
        } else {
            localCurrencySource.getExchangeRates()
        }
    }

    private suspend fun updateRefreshSchedule(resource: String) {
        refreshSchedulesDao.upsert(
            RefreshSchedules(
                resource = resource,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private suspend fun shouldFetchFromRemote(resource: String): Boolean {
        return System.currentTimeMillis() - (refreshSchedulesDao.getLastUpdatedTimestamp(resource)
            ?: 0) >
                TimeUnit.MINUTES.toMillis(NetworkConst.REFRESH_TIMEOUT)
    }


}