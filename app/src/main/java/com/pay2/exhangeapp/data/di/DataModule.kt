package com.pay2.exhangeapp.data.di

import android.content.Context
import android.net.ConnectivityManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.pay2.exhangeapp.BuildConfig
import com.pay2.exhangeapp.common.CoroutineDispatchers
import com.pay2.exhangeapp.common.CoroutineDispatchersProvider
import com.pay2.exhangeapp.common.NetworkConst
import com.pay2.exhangeapp.common.NetworkUtil
import com.pay2.exhangeapp.data.source.remote.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            chain.proceed(
                chain.request().newBuilder().url(
                    chain.request().url.newBuilder().addQueryParameter(
                        NetworkConst.APP_ID_QUERY_PARAM, BuildConfig.OPEN_EXCHANGE_APP_ID
                    ).build()
                ).build()
            )
        }
        val okHttpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(NetworkConst.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConst.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConst.READ_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
        return if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            okHttpClientBuilder
                .addInterceptor(loggingInterceptor)
                .build()
        } else okHttpClientBuilder
            .build()
    }

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(ConnectivityManager::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkUtil(connectivityManager: ConnectivityManager): NetworkUtil {
        return NetworkUtil(connectivityManager)
    }

    @Provides
    @Singleton
    fun provideCoroutineDispatchers(): CoroutineDispatchers {
        return CoroutineDispatchersProvider
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun getApiService(okHttpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create()
    }

}