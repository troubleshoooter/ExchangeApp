package com.pay2.exhangeapp.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pay2.exhangeapp.data.source.local.dao.CurrencyDao
import com.pay2.exhangeapp.data.source.local.dao.ExchangeRatesDao
import com.pay2.exhangeapp.data.source.local.dao.RefreshSchedulesDao
import com.pay2.exhangeapp.data.source.local.entity.CurrencyEntity
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRatesEntity
import com.pay2.exhangeapp.data.source.local.entity.RefreshSchedulesEntity

@Database(
    entities = [CurrencyEntity::class, ExchangeRatesEntity::class, RefreshSchedulesEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ExchangeDatabase : RoomDatabase() {

    abstract val currencyDao: CurrencyDao
    abstract val exchangeRatesDao: ExchangeRatesDao
    abstract val refreshSchedulesDao: RefreshSchedulesDao

}