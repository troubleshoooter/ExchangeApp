package com.pay2.exhangeapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pay2.exhangeapp.data.models.Currency
import com.pay2.exhangeapp.data.models.ExchangeRates
import com.pay2.exhangeapp.data.repositories.CurrencyRepository
import com.pay2.exhangeapp.presentation.ui.home.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private var selectedCurrency: Currency? = savedStateHandle[SELECTED_CURRENCY_KEY]
    private var amount: Double? = savedStateHandle[ENTERED_AMOUNT_KEY]
    private var conversionJob: Job? = null
    private val homeUiState: MutableStateFlow<HomeUiState> =
        MutableStateFlow(HomeUiState())

    companion object {
        private const val DEBOUNCE = 500L
        private const val SELECTED_CURRENCY_KEY = "selected_currency"
        private const val ENTERED_AMOUNT_KEY = "entered_amount"
    }

    private fun fetchCurrencies() {
        viewModelScope.launch(Dispatchers.IO) {
            currencyRepository.getCurrencies().collectLatest { currencyList ->
                if (currencyList.isSuccess) {
                    homeUiState.update {
                        it.copy(
                            isLoading = false,
                            currencies = currencyList.getOrDefault(emptyList())
                        )
                    }
                } else {
                    homeUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = currencyList.exceptionOrNull()?.message
                        )
                    }
                }
            }
        }
    }

    fun getSelectedCurrency(): Currency? {
        return selectedCurrency
    }

    fun setSelectedCurrency(currency: Currency?) {
        savedStateHandle[SELECTED_CURRENCY_KEY] = currency
        selectedCurrency = currency
    }

    fun getHomeUiState(): StateFlow<HomeUiState> {
        if (homeUiState.value.currencies.isEmpty()) {
            fetchCurrencies()
        }
        if (savedStateHandle.get<Double>(ENTERED_AMOUNT_KEY) != null) {
            getConversions(savedStateHandle[ENTERED_AMOUNT_KEY] ?: 1.0)
        }
        return homeUiState
    }

    fun getConversions(amount: Double) {
        homeUiState.update {
            it.copy(isLoading = true)
        }
        conversionJob?.cancel()
        selectedCurrency?.code?.let { code ->
            conversionJob = viewModelScope.launch(Dispatchers.IO) {
                delay(DEBOUNCE)
                savedStateHandle[ENTERED_AMOUNT_KEY] = amount
                if (homeUiState.value.exchangeRates.isEmpty()) {
                    currencyRepository.getExchangeRates(code).collectLatest {
                        if (it.isSuccess) {
                            originalExchangeRates = it.getOrDefault(emptyList())
                        } else {
                            homeUiState.update { uiState ->
                                uiState.copy(
                                    isLoading = false,
                                    errorMessage = it.exceptionOrNull()?.message
                                )
                            }
                        }
                    }
                }
                withContext(Dispatchers.Default) {
                    homeUiState.update { uiState ->
                        uiState.copy(
                            isLoading = false,
                            exchangeRates = transformListWithCalculatedRates(
                                amount,
                                code,
                                originalExchangeRates
                            )
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
        homeUiState.update {
            it.copy(exchangeRates = emptyList())
        }
    }
}