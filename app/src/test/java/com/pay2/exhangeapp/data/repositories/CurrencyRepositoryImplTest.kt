package com.pay2.exhangeapp.data.repositories

import com.pay2.exhangeapp.common.NetworkConst
import com.pay2.exhangeapp.common.NetworkUtil
import com.pay2.exhangeapp.data.source.local.LocalCurrencyDataSource
import com.pay2.exhangeapp.data.source.local.dao.RefreshSchedulesDao
import com.pay2.exhangeapp.data.source.local.entity.CurrencyEntity
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRatesEntity
import com.pay2.exhangeapp.data.source.local.entity.toExternalModel
import com.pay2.exhangeapp.data.source.remote.RemoteCurrencyDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class CurrencyRepositoryImplTest {

    private val remoteCurrencySource = mockk<RemoteCurrencyDataSource>()
    private val localCurrencySource = mockk<LocalCurrencyDataSource>()

    private val refreshSchedulesDao = mockk<RefreshSchedulesDao>()

    private val networkUtil = mockk<NetworkUtil>()

    private lateinit var repository: CurrencyRepositoryImpl

    @Before
    fun setUp() {
        repository = CurrencyRepositoryImpl(
            localCurrencySource,
            remoteCurrencySource,
            refreshSchedulesDao,
            networkUtil
        )
    }

    @Test
    fun `getCurrencies should fetch from remote and cache if internet is connected and refresh time is expired`() =
        runTest {
            val mockCurrencies = listOf(CurrencyEntity(code = "USD", name = "US Dollar"))
            coEvery { networkUtil.isConnectedToInternet() } returns true
            coEvery { refreshSchedulesDao.getLastUpdatedTimestamp(NetworkConst.EndPoints.CURRENCIES) } returns
                    System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(NetworkConst.REFRESH_TIMEOUT + 10)

            coEvery { remoteCurrencySource.getCurrencies() } returns mockCurrencies
            coEvery { localCurrencySource.saveCurrencies(mockCurrencies) } returns Unit
            coEvery {
                refreshSchedulesDao.upsert(any())
            } returns Unit
            val result = repository.getCurrencies().first()

            assertEquals(Result.success(mockCurrencies.map { it.toExternalModel() }), result)

            coVerify {
                remoteCurrencySource.getCurrencies()
                localCurrencySource.saveCurrencies(mockCurrencies)
                refreshSchedulesDao.upsert(
                    match {
                        it.resource == NetworkConst.EndPoints.CURRENCIES
                    }
                )
            }
        }

    @Test
    fun `getExchangeRates should fetch from remote and cache if internet is connected and refresh time is expired`() =
        runTest {
            val baseCurrency = "USD"
            val mockExchangeRates = listOf(ExchangeRatesEntity(code = baseCurrency, rate = 100.0))
            coEvery { networkUtil.isConnectedToInternet() } returns true
            coEvery {
                refreshSchedulesDao.getLastUpdatedTimestamp(NetworkConst.EndPoints.EXCHANGE_RATES)
            } returns System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(NetworkConst.REFRESH_TIMEOUT + 10)

            coEvery { remoteCurrencySource.getExchangeRates(any()) } returns mockExchangeRates
            coEvery { localCurrencySource.saveExchangeRates(mockExchangeRates) } returns Unit
            coEvery {
                refreshSchedulesDao.upsert(any())
            } returns Unit
            val result = repository.getExchangeRates(baseCurrency).first()

            assertEquals(Result.success(mockExchangeRates.map { it.toExternalModel() }), result)

            coVerify {
                remoteCurrencySource.getExchangeRates(baseCurrency)
                localCurrencySource.saveExchangeRates(mockExchangeRates)
                refreshSchedulesDao.upsert(
                    match {
                        it.resource == "${NetworkConst.EndPoints.EXCHANGE_RATES}/$baseCurrency"
                    }
                )
            }
        }

    @Test
    fun `getCurrencies should fetch from local if internet is connected and refresh time is valid`() =
        runTest {
            val mockCurrencies = listOf(CurrencyEntity(code = "USD", name = "US Dollar"))

            coEvery { networkUtil.isConnectedToInternet() } returns true
            coEvery {
                refreshSchedulesDao.getLastUpdatedTimestamp(NetworkConst.EndPoints.CURRENCIES)
            } returns System.currentTimeMillis()
            coEvery { localCurrencySource.getCurrencies() } returns mockCurrencies
            coEvery { localCurrencySource.saveCurrencies(mockCurrencies) } returns Unit
            coEvery { remoteCurrencySource.getCurrencies() } returns mockCurrencies

            val result = repository.getCurrencies().first()

            assertEquals(Result.success(mockCurrencies.map { it.toExternalModel() }), result)

            coVerify {
                localCurrencySource.getCurrencies()
            }
            coVerify(exactly = 0) { remoteCurrencySource.getCurrencies() }

        }

    @Test
    fun `getExchangeRates should fetch from local if internet is connected and refresh time is valid`() =
        runTest {
            val baseCurrency = "USD"
            val mockExchangeRates = listOf(ExchangeRatesEntity(code = baseCurrency, rate = 100.0))

            coEvery { networkUtil.isConnectedToInternet() } returns true
            coEvery {
                refreshSchedulesDao.getLastUpdatedTimestamp(NetworkConst.EndPoints.EXCHANGE_RATES)
            } returns System.currentTimeMillis()
            coEvery { localCurrencySource.getExchangeRates() } returns mockExchangeRates
            coEvery { localCurrencySource.saveExchangeRates(mockExchangeRates) } returns Unit
            coEvery { remoteCurrencySource.getExchangeRates(baseCurrency) } returns mockExchangeRates

            val result = repository.getExchangeRates(baseCurrency).first()

            assertEquals(Result.success(mockExchangeRates.map { it.toExternalModel() }), result)

            coVerify {
                localCurrencySource.getExchangeRates()
            }
            coVerify(exactly = 0) { remoteCurrencySource.getExchangeRates(baseCurrency) }

        }

    @Test
    fun `getCurrencies should fetch from local if internet is disconnected`() = runTest {
        val mockCurrencies = listOf(CurrencyEntity(code = "USD", name = "US Dollar"))

        coEvery { networkUtil.isConnectedToInternet() } returns false
        coEvery { localCurrencySource.getCurrencies() } returns mockCurrencies
        coEvery {
            refreshSchedulesDao.getLastUpdatedTimestamp(NetworkConst.EndPoints.CURRENCIES)
        } returns 0L
        val result = repository.getCurrencies().first()

        assertEquals(Result.success(mockCurrencies.map { it.toExternalModel() }), result)

        coVerify {
            localCurrencySource.getCurrencies()
        }
        coVerify(exactly = 0) { remoteCurrencySource.getCurrencies() }

    }

    @Test
    fun `getExchangeRates should fetch from local if internet is disconnected`() = runTest {
        val baseCurrency = "USD"
        val mockExchangeRates = listOf(ExchangeRatesEntity(code = baseCurrency, rate = 100.0))

        coEvery { networkUtil.isConnectedToInternet() } returns false
        coEvery { localCurrencySource.getExchangeRates() } returns mockExchangeRates
        coEvery {
            refreshSchedulesDao.getLastUpdatedTimestamp(NetworkConst.EndPoints.EXCHANGE_RATES)
        } returns 0L
        val result = repository.getExchangeRates(baseCurrency).first()

        assertEquals(Result.success(mockExchangeRates.map { it.toExternalModel() }), result)

        coVerify {
            localCurrencySource.getExchangeRates()
        }
        coVerify(exactly = 0) { remoteCurrencySource.getExchangeRates(baseCurrency) }

    }

    @Test
    fun `getCurrencies should return with fail response if any error occurred`() = runTest {
        val exception = Throwable("Something Went Wrong")
        coEvery { networkUtil.isConnectedToInternet() } returns false
        coEvery { localCurrencySource.getCurrencies() } throws exception
        coEvery {
            refreshSchedulesDao.getLastUpdatedTimestamp(NetworkConst.EndPoints.CURRENCIES)
        } returns 0L
        val result = repository.getCurrencies().first()

        assertEquals(Result.failure<Throwable>(exception), result)
    }

    @Test
    fun `getExchangeRates should return with fail response if any error occurred`() = runTest {
        val baseCurrency = "USD"
        val exception = Throwable("Something Went Wrong")

        coEvery { networkUtil.isConnectedToInternet() } returns false
        coEvery { localCurrencySource.getExchangeRates() } throws exception
        coEvery {
            refreshSchedulesDao.getLastUpdatedTimestamp(NetworkConst.EndPoints.EXCHANGE_RATES)
        } returns 0L
        val result = repository.getExchangeRates(baseCurrency).first()

        assertEquals(Result.failure<Throwable>(exception), result)
    }
}