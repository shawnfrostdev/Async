package com.shawnfrost.async.di

import com.shawnfrost.async.data.api.FMAService
import com.shawnfrost.async.data.api.InternetArchiveService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    @Named("fmaRetrofit")
    fun provideFMARetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://freemusicarchive.org/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("iaRetrofit")
    fun provideInternetArchiveRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://archive.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideFMAService(@Named("fmaRetrofit") retrofit: Retrofit): FMAService {
        return retrofit.create(FMAService::class.java)
    }

    @Provides
    @Singleton
    fun provideInternetArchiveService(@Named("iaRetrofit") retrofit: Retrofit): InternetArchiveService {
        return retrofit.create(InternetArchiveService::class.java)
    }
} 