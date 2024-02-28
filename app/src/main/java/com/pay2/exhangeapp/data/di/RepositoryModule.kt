package com.pay2.exhangeapp.data.di

import android.content.Context
import androidx.room.Room
import com.pay2.exhangeapp.common.DatabaseConst
import com.pay2.exhangeapp.common.NetworkUtil
import com.pay2.exhangeapp.data.repositories.CurrencyRepository
import com.pay2.exhangeapp.data.repositories.CurrencyRepositoryImpl
import com.pay2.exhangeapp.data.source.local.LocalCurrencyDataSource
import com.pay2.exhangeapp.data.source.local.dao.CurrencyDao
import com.pay2.exhangeapp.data.source.local.dao.ExchangeRatesDao
import com.pay2.exhangeapp.data.source.local.dao.RefreshSchedulesDao
import com.pay2.exhangeapp.data.source.local.db.ExchangeDatabase
import com.pay2.exhangeapp.data.source.remote.RemoteCurrencyDataSource
import com.pay2.exhangeapp.data.source.remote.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun provideExchangeDatabase(@ApplicationContext appContext: Context): ExchangeDatabase {
        return Room.databaseBuilder(
            appContext,
            ExchangeDatabase::class.java,
            DatabaseConst.EXCHANGE_APP_DB
        ).build()
    }

    @Provides
    @Singleton
    fun provideCurrencyDao(db: ExchangeDatabase): CurrencyDao {
        return db.currencyDao
    }

    @Provides
    @Singleton
    fun provideExchangeRatesDao(db: ExchangeDatabase): ExchangeRatesDao {
        return db.exchangeRatesDao
    }

    @Provides
    @Singleton
    fun provideRefreshScheduleDao(db: ExchangeDatabase): RefreshSchedulesDao {
        return db.refreshSchedulesDao
    }

    @Provides
    fun provideLocalCurrencyDataSource(
        currencyDao: CurrencyDao,
        exchangeRatesDao: ExchangeRatesDao
    ): LocalCurrencyDataSource {
        return LocalCurrencyDataSource(currencyDao, exchangeRatesDao)
    }

    @Provides
    fun provideRemoteCurrencyDataSource(apiService: ApiService): RemoteCurrencyDataSource {
        return RemoteCurrencyDataSource(apiService)
    }

    @Provides
    fun provideCurrencyRepository(
        localCurrencyDataSource: LocalCurrencyDataSource,
        remoteCurrencyDataSource: RemoteCurrencyDataSource,
        refreshSchedulesDao: RefreshSchedulesDao,
        networkUtil: NetworkUtil
    ): CurrencyRepository {
        return CurrencyRepositoryImpl(
            localCurrencyDataSource,
            remoteCurrencyDataSource,
            refreshSchedulesDao,
            networkUtil
        )
    }
}