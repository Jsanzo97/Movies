package com.example.movielist.di.remote

import com.example.movielist.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

const val TIMEOUT = 15L
const val BASIC_OK_HTTP_CLIENT = "BASIC_OK_HTTP_CLIENT"
const val APP_OK_HTTP_CLIENT = "APP_OK_HTTP_CLIENT"
const val APP_WS = "APP_WS"

val remoteModule = module {

    single(named(BASIC_OK_HTTP_CLIENT)) {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    single(named(APP_WS)) {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .client(get(named(APP_OK_HTTP_CLIENT)))
            .baseUrl(BuildConfig.SERVER_ENDPOINT)
            .addConverterFactory(Json.asConverterFactory(contentType))
            .build()
    }
}