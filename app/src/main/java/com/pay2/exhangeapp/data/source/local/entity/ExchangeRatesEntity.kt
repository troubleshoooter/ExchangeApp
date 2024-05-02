package com.pay2.exhangeapp.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pay2.exhangeapp.data.models.ExchangeRates

@Entity(tableName = "exchange_rates")
data class ExchangeRatesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: String,
    val rate: Double
)

fun ExchangeRatesEntity.toExternalModel() = ExchangeRates(
    code = code,
    rate = rate
)