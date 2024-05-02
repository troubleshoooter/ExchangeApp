package com.pay2.exhangeapp.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pay2.exhangeapp.data.models.Currency

@Entity(tableName = "currency")
data class CurrencyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: String,
    val name: String
)

fun CurrencyEntity.toExternalModel() = Currency(
    code = code,
    name = name
)