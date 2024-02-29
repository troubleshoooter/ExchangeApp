package com.pay2.exhangeapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pay2.exhangeapp.data.models.Currency
import com.pay2.exhangeapp.data.models.ExchangeRates
import com.pay2.exhangeapp.data.repositories.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var originalExchangeRates: List<ExchangeRates> = listOf()
    private var currencies: MutableStateFlow<ImmutableList<Currency>?> = MutableStateFlow(null)
    private var exchangeRates: MutableStateFlow<List<ExchangeRates>?> = MutableStateFlow(null)
    private var error: MutableStateFlow<String?> = MutableStateFlow(null)
    private var selectedCurrency: Currency? = savedStateHandle[SELECTED_CURRENCY_KEY]
    private var amount: Double? = savedStateHandle[ENTERED_AMOUNT_KEY]
    private var conversionJob: Job? = null

    companion object {
        private const val DEBOUNCE = 500L
        private const val SELECTED_CURRENCY_KEY = "selected_currency"
        private const val ENTERED_AMOUNT_KEY = "entered_amount"
    }

    private fun fetchCurrencies() {
        viewModelScope.launch(Dispatchers.IO) {
            currencyRepository.getCurrencies().collectLatest { result ->
                if (result.isSuccess) {
                    currencies.update {
                        result.getOrNull()?.toPersistentList() ?: persistentListOf()
                    }
                } else {
                    error.update {
                        result.exceptionOrNull()?.message
                    }
                }
            }
        }
    }

    fun getSelectedCurrency(): Currency? {
        return selectedCurrency
    }

    fun fetchData() {
        if (currencies.value.isNullOrEmpty()) {
            fetchCurrencies()
        }
        if (savedStateHandle.get<Double>(ENTERED_AMOUNT_KEY) != null) {
            getConversions(savedStateHandle[ENTERED_AMOUNT_KEY] ?: 1.0)
        }
    }

    fun getCurrencies(): StateFlow<ImmutableList<Currency>?> {
        return currencies
    }

    fun getExchangeRates(): StateFlow<List<ExchangeRates>?> {
        return exchangeRates
    }

    fun getError(): StateFlow<String?> {
        return error
    }

    fun setSelectedCurrency(currency: Currency?) {
        savedStateHandle[SELECTED_CURRENCY_KEY] = currency
        selectedCurrency = currency
    }

    fun getConversions(amount: Double) {
        conversionJob?.cancel()
        selectedCurrency?.code?.let { code ->
            conversionJob = viewModelScope.launch(Dispatchers.IO) {
                delay(DEBOUNCE)
                savedStateHandle[ENTERED_AMOUNT_KEY] = amount
                if (originalExchangeRates.isEmpty()) {
                    currencyRepository.getExchangeRates(code).collectLatest { result ->
                        if (result.isSuccess) {
                            originalExchangeRates = result.getOrDefault(emptyList())
                        } else {
                            error.update {
                                result.exceptionOrNull()?.message
                            }
                        }
                    }
                }
                withContext(Dispatchers.Default) {
                    exchangeRates.update {
                        transformListWithCalculatedRates(
                            amount,
                            code,
                            originalExchangeRates
                        )
                    }
                }
            }
        }
    }

    private fun transformListWithCalculatedRates(
        amount: Double,
        base: String,
        list: List<ExchangeRates>
    ): List<ExchangeRates> {
        // if the base is already USD we already have the data :)
        return if (base.equals("USD", ignoreCase = true)) {
            return list.map { it.copy(code = it.code, rate = it.rate * amount) }
        } else {
            // calculating for other base currency using simple formula based on USD rates
            // i.e-> INR to GBP rate = amount / (USD to GBP rate) * (USD to INR rate)
            val usdToBaseRate =
                list.findLast { it.code.equals(base, ignoreCase = true) }?.rate ?: 1.0
            list.map {
                it.copy(
                    code = it.code,
                    rate = amount / usdToBaseRate * (list.findLast { c -> it.code == c.code }?.rate
                        ?: 1.0)
                )
            }
        }
    }

    fun clearList() {
        conversionJob?.cancel()
        exchangeRates.update {
            null
        }
    }
}