package com.pay2.exhangeapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pay2.exhangeapp.data.repositories.CurrencyRepository
import com.pay2.exhangeapp.data.source.local.entity.Currency
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val currencies = MutableLiveData<List<Currency>>()
    private val exchangeRates = MutableLiveData<List<ExchangeRates>>(emptyList())
    private var originalExchangeRates: List<ExchangeRates> = listOf()
    private var selectedCurrency: Currency? = null
    private var conversionJob: Job? = null

    companion object {
        private const val DEBOUNCE = 1000L
    }

    private fun fetchCurrencies() {
        viewModelScope.launch(Dispatchers.IO) {
            currencies.postValue(currencyRepository.getCurrencies())
        }
    }

    fun setSelectedCurrency(currency: Currency?) {
        selectedCurrency = currency
    }

    fun getSelectedCurrency() = selectedCurrency

    fun getExchangeRates(): LiveData<List<ExchangeRates>> = exchangeRates

    fun getCurrencies(): LiveData<List<Currency>> {
        if (currencies.value.isNullOrEmpty()) {
            fetchCurrencies()
        }
        return currencies
    }

    fun getConversions(amount: Double) {
        conversionJob?.cancel()
        selectedCurrency?.code?.let { code ->
            conversionJob = viewModelScope.launch(Dispatchers.IO) {
                delay(DEBOUNCE)
                if (exchangeRates.value.isNullOrEmpty()) {
                    originalExchangeRates = currencyRepository.getExchangeRates(code)
                }
                launch(Dispatchers.Default) {
                    exchangeRates.postValue(
                        transformListWithCalculatedRates(
                            amount,
                            code,
                            originalExchangeRates
                        )
                    )
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
        exchangeRates.postValue(emptyList())
    }
}