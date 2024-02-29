package com.pay2.exhangeapp.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pay2.exhangeapp.data.source.local.entity.CurrencyEntity

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(currencies: List<CurrencyEntity>)

    @Query("SELECT * from currency")
    suspend fun getCurrencies(): List<CurrencyEntity>
}