package com.pay2.exhangeapp.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExchangeRates(
    val code: String,
    val rate: Double
) : Parcelable