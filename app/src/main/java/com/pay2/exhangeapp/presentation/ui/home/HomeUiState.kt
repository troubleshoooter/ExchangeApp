package com.pay2.exhangeapp.presentation.ui.home

import com.pay2.exhangeapp.data.source.local.entity.Currency
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRates

data class HomeUiState(
    val isLoading: Boolean = true,
    val currencies: List<Currency> = emptyList(),
    val exchangeRates: List<ExchangeRates> = emptyList(),
    val errorMessage: String? = null
)