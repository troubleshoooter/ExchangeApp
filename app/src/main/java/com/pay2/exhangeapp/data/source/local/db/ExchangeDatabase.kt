package com.pay2.exhangeapp.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pay2.exhangeapp.data.source.local.dao.CurrencyDao
import com.pay2.exhangeapp.data.source.local.dao.ExchangeRatesDao
import com.pay2.exhangeapp.data.source.local.dao.RefreshSchedulesDao
import com.pay2.exhangeapp.data.source.local.entity.Currency
import com.pay2.exhangeapp.data.source.local.entity.ExchangeRates
import com.pay2.exhangeapp.data.source.local.entity.RefreshSchedules

@Database(
    entities = [Currency::class, ExchangeRates::class, RefreshSchedules::class],
    version = 1,
    exportSchema = false
)
abstract class ExchangeDatabase : RoomDatabase() {

    abstract val currencyDao: CurrencyDao
    abstract val exchangeRatesDao: ExchangeRatesDao
    abstract val refreshSchedulesDao: RefreshSchedulesDao

}