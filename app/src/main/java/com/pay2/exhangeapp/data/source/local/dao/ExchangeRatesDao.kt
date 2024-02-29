package com.pay2.exhangeapp.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRatesEntity

@Dao
interface ExchangeRatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(currencies: List<ExchangeRatesEntity>)

    @Query("SELECT * from exchange_rates")
    suspend fun getExchangeRates(): List<ExchangeRatesEntity>
}