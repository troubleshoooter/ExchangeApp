package com.pay2.exhangeapp.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRates(
    @PrimaryKey
    val code: String,
    val rate: Double
)