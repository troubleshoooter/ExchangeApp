package com.pay2.exhangeapp.data.repositories

import com.pay2.exhangeapp.common.NetworkConst
import com.pay2.exhangeapp.common.NetworkUtil
import com.pay2.exhangeapp.data.models.Currency
import com.pay2.exhangeapp.data.models.ExchangeRates
import com.pay2.exhangeapp.data.source.local.LocalCurrencyDataSource
import com.pay2.exhangeapp.data.source.local.dao.RefreshSchedulesDao
import com.pay2.exhangeapp.data.source.local.entity.RefreshSchedulesEntity
import com.pay2.exhangeapp.data.source.local.entity.toExternalModel
import com.pay2.exhangeapp.data.source.remote.RemoteCurrencyDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class CurrencyRepositoryImpl @Inject constructor(
    private val localCurrencySource: LocalCurrencyDataSource,
    private val remoteCurrencySource: RemoteCurrencyDataSource,
    private val refreshSchedulesDao: RefreshSchedulesDao,
    private val networkUtil: NetworkUtil
) : CurrencyRepository {

    override fun getCurrencies(): Flow<Result<List<Currency>>> = flow {
        if (shouldFetchFromRemote(NetworkConst.EndPoints.CURRENCIES) && networkUtil.isConnectedToInternet()) {
            val currencies = remoteCurrencySource.getCurrencies()
            localCurrencySource.saveCurrencies(currencies)
            updateRefreshSchedule(NetworkConst.EndPoints.CURRENCIES)
            emit(Result.success(currencies.map { it.toExternalModel() }))
        } else {
            emit(Result.success(localCurrencySource.getCurrencies().map { it.toExternalModel() }))
        }
    }.catch {
        emit(Result.failure(it))
    }

    override fun getExchangeRates(sourceCurrency: String): Flow<Result<List<ExchangeRates>>> =
        flow {
            if (shouldFetchFromRemote(NetworkConst.EndPoints.EXCHANGE_RATES)
                && networkUtil.isConnectedToInternet()
            ) {
                val exchangeRates = remoteCurrencySource.getExchangeRates(sourceCurrency)
                localCurrencySource.saveExchangeRates(exchangeRates)
                // future proofing for other currencies
                updateRefreshSchedule(NetworkConst.EndPoints.EXCHANGE_RATES + "/$sourceCurrency")
                emit(Result.success(exchangeRates.map { it.toExternalModel() }))
            } else {
                emit(
                    Result.success(
                        localCurrencySource.getExchangeRates().map { it.toExternalModel() })
                )
            }
        }.catch {
            emit(Result.failure(it))
        }

    private suspend fun updateRefreshSchedule(resource: String) {
        refreshSchedulesDao.upsert(
            RefreshSchedulesEntity(
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