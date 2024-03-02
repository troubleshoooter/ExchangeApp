package com.pay2.exhangeapp.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.pay2.exhangeapp.data.models.Currency
import com.pay2.exhangeapp.data.models.ExchangeRates
import com.pay2.exhangeapp.data.repositories.CurrencyRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private var currencyRepository = mockk<CurrencyRepository>()

    private lateinit var viewModel: MainViewModel
    private val savedStateHandle = SavedStateHandle()
    private val dispatchers = CoroutineTestDispatcherProvider


    @get:Rule
    var rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatchers.testDispatcher)
        viewModel = MainViewModel(currencyRepository, savedStateHandle, dispatchers)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getSelectedCurrency should return null when there is no value in saveStateHandle`() {
        val selectedCurrency = viewModel.getSelectedCurrency()
        assertNull(selectedCurrency)
    }

    @Test
    fun `getSelectedCurrency should not return null when there is value in saveStateHandle`() {
        savedStateHandle["selected_currency"] = mockk<Currency>()
        viewModel = MainViewModel(currencyRepository, savedStateHandle, dispatchers)
        assertNotNull(viewModel.getSelectedCurrency())
    }

    @Test
    fun `fetchCurrencies should update currencies flow with successful response`() {
        coEvery { currencyRepository.getCurrencies() } returns flowOf(
            Result.success(listOf(Currency("INR", "Indian Rupees")))
        )

        viewModel.fetchData()
        dispatchers.testDispatcher.scheduler.advanceUntilIdle()
        val value = viewModel.getCurrencies().value
        assertNotNull(value)
        assertEquals(1, value?.size)
        assertEquals("INR", value?.get(0)?.code)
    }

    @Test
    fun `fetchData should not update currencies is it is initialised`() {
        coEvery { currencyRepository.getCurrencies() } returns flowOf(
            Result.success(listOf(Currency("INR", "Indian Rupees")))
        )
        viewModel.fetchData()
        dispatchers.testDispatcher.scheduler.advanceUntilIdle()
        val value = viewModel.getCurrencies().value
        assertNotNull(value)
        assertEquals(1, value?.size)
        assertEquals("INR", value?.get(0)?.code)

        viewModel.fetchData()
        dispatchers.testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { currencyRepository.getCurrencies() }
    }

    @Test
    fun `fetchData should call getConversion if there is value in savedStateHandle`() {
        val currency = "INR"
        val amount = 100.0
        savedStateHandle["entered_amount"] = amount
        coEvery { currencyRepository.getCurrencies() } returns flowOf(
            Result.success(listOf(Currency("INR", "Indian Rupees")))
        )
        coEvery { currencyRepository.getExchangeRates(currency) } returns flowOf(
            Result.success(listOf(ExchangeRates(currency, 1.0)))
        )

        viewModel.setSelectedCurrency(Currency(currency, "Indian Rupees"))
        viewModel.fetchData()
        dispatchers.testDispatcher.scheduler.advanceUntilIdle()
        val exchangeRatesValue = viewModel.getExchangeRates().value
        coVerify(exactly = 1) { currencyRepository.getExchangeRates(currency) }
        assertNotNull(exchangeRatesValue)
        assertEquals(1, exchangeRatesValue!!.size)
        assertEquals(100.0, exchangeRatesValue[0].rate, 0.0)
    }

    @Test
    fun `fetchCurrencies should update error flow with error response`() {
        coEvery { currencyRepository.getCurrencies() } returns flowOf(
            Result.failure(Exception("Network error"))
        )

        viewModel.fetchData()
        dispatchers.testDispatcher.scheduler.advanceUntilIdle()
        val errorValue = viewModel.getError().value
        assertNotNull(errorValue)
        assertEquals("Network error", errorValue)
    }

    @Test
    fun `getConversions should update exchangeRates flow with successful response`() = runTest {
        val currency = "INR"
        val amount = 100.0

        coEvery { currencyRepository.getExchangeRates(currency) } returns flowOf(
            Result.success(listOf(ExchangeRates(currency, 1.0)))
        )

        viewModel.setSelectedCurrency(Currency(currency, "Indian Rupees"))
        viewModel.getConversions(amount)
        dispatchers.testDispatcher.scheduler.advanceUntilIdle()
        val exchangeRatesValue = viewModel.getExchangeRates().value
        assertNotNull(exchangeRatesValue)
        assertEquals(1, exchangeRatesValue!!.size)
        assertEquals(100.0, exchangeRatesValue[0].rate, 0.0)
    }

    @Test
    fun `getConversions should return rates as per the USD conversion Rates`() = runTest {
        val currency = "INR"
        val amount = 100.0

        coEvery { currencyRepository.getExchangeRates(any()) } returns flowOf(
            Result.success(listOf(ExchangeRates(currency, 1.0)))
        )

        viewModel.setSelectedCurrency(Currency("USD", "US Dollar"))
        viewModel.getConversions(amount)
        dispatchers.testDispatcher.scheduler.advanceUntilIdle()
        val exchangeRatesValue = viewModel.getExchangeRates().value
        assertNotNull(exchangeRatesValue)
        assertEquals(1, exchangeRatesValue!!.size)
        assertEquals(100.0, exchangeRatesValue[0].rate, 0.0)
    }

    @Test
    fun `getConversions should return rates calculated using USD conversion Rates`() = runTest {
        val amount = 100.0

        val baseCurrency = ExchangeRates("INR", 82.83)
        val targetCurrency = ExchangeRates("GBP", 0.79)

        coEvery { currencyRepository.getExchangeRates(baseCurrency.code) } returns flowOf(
            Result.success(listOf(baseCurrency, targetCurrency))
        )

        viewModel.setSelectedCurrency(Currency("INR", "Great British Pound"))
        viewModel.getConversions(amount)
        dispatchers.testDispatcher.scheduler.advanceUntilIdle()
        val exchangeRatesValue = viewModel.getExchangeRates().value
        assertNotNull(exchangeRatesValue)
        assertEquals(2, exchangeRatesValue!!.size)
        assertEquals(amount, exchangeRatesValue[0].rate, 0.0)
        assertEquals(
            amount * targetCurrency.rate / baseCurrency.rate,
            exchangeRatesValue[1].rate,
            0.0
        )
    }

    @Test
    fun `getConversions should fetch from repository if exchangeRatesList is nullOrEmpty`() =
        runTest {
            val currency = "INR"
            val amount = 100.0

            coEvery { currencyRepository.getExchangeRates(currency) } returns flowOf(
                Result.success(listOf(ExchangeRates(currency, 1.0)))
            )

            viewModel.setSelectedCurrency(Currency(currency, "Indian Rupees"))
            viewModel.getConversions(amount)
            dispatchers.testDispatcher.scheduler.advanceUntilIdle()
            val exchangeRatesValue = viewModel.getExchangeRates().value
            coVerify(exactly = 1) { currencyRepository.getExchangeRates(currency) }
            assertNotNull(exchangeRatesValue)
            assertEquals(1, exchangeRatesValue!!.size)
            assertEquals(100.0, exchangeRatesValue[0].rate, 0.0)
        }

    @Test
    fun `getConversions should not fetch from repository if exchangeRatesList is initialised`() =
        runTest {
            val currency = "INR"
            val amount = 100.0

            coEvery { currencyRepository.getExchangeRates(currency) } returns flowOf(
                Result.success(listOf(ExchangeRates(currency, 1.0)))
            )

            viewModel.setSelectedCurrency(Currency(currency, "Indian Rupees"))
            viewModel.getConversions(amount)
            dispatchers.testDispatcher.scheduler.advanceUntilIdle()
            val exchangeRatesValue = viewModel.getExchangeRates().value
            assertNotNull(exchangeRatesValue)
            assertEquals(1, exchangeRatesValue!!.size)
            assertEquals(100.0, exchangeRatesValue[0].rate, 0.0)
            viewModel.getConversions(amount)
            dispatchers.testDispatcher.scheduler.advanceUntilIdle()
            coVerify(exactly = 1) { currencyRepository.getExchangeRates(currency) }
        }

    @Test
    fun `getConversions should not update exchangeRates if selectedCurrency is null`() =
        runTest {
            val currency = "INR"
            val amount = 100.0

            coEvery { currencyRepository.getExchangeRates(any()) } returns flowOf(
                Result.success(listOf(ExchangeRates(currency, 1.0)))
            )

            viewModel.setSelectedCurrency(null)
            viewModel.getConversions(amount)
            val exchangeRatesValue = viewModel.getExchangeRates().value
            assertNull(exchangeRatesValue)
        }

    @Test
    fun `getConversions should update error flow with error response`() {
        coEvery { currencyRepository.getExchangeRates(any()) } returns flowOf(
            Result.failure(Exception("Network error"))
        )
        viewModel.setSelectedCurrency(Currency("INR", "Indian Rupees"))

        viewModel.getConversions(10.0)
        dispatchers.testDispatcher.scheduler.advanceUntilIdle()
        val errorValue = viewModel.getError().value
        assertNotNull(errorValue)
        assertEquals("Network error", errorValue)
    }

    @Test
    fun `clearList should cancel job and resets exchangeRates`() {
        val currency = "INR"
        val amount = 100.0

        coEvery { currencyRepository.getExchangeRates(currency) } returns flowOf(
            Result.success(listOf(ExchangeRates(currency, 1.0)))
        )

        viewModel.setSelectedCurrency(Currency(currency, "Indian Rupees"))
        viewModel.getConversions(amount)
        dispatchers.testDispatcher.scheduler.advanceUntilIdle()
        val exchangeRatesValue = viewModel.getExchangeRates().value
        assertNotNull(exchangeRatesValue)
        assertEquals(1, exchangeRatesValue!!.size)
        assertEquals(100.0, exchangeRatesValue[0].rate, 0.0)

        viewModel.clearList()
        assertNull(viewModel.getExchangeRates().value)
    }


}