package com.pay2.exhangeapp.presentation.ui.home

import android.os.Parcelable
import com.pay2.exhangeapp.data.models.Currency
import com.pay2.exhangeapp.data.models.ExchangeRates
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeUiState(
    val isLoading: Boolean = true,
    val currencies: List<Currency> = emptyList(),
    val exchangeRates: List<ExchangeRates> = emptyList(),
    val errorMessage: String? = null
) : Parcelable