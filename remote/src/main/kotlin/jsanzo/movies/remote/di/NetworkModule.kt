package jsanzo.movies.remote.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import jsanzo.movies.remote.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

const val TIMEOUT = 15L
const val BASIC_OK_HTTP_CLIENT = "BASIC_OK_HTTP_CLIENT"
const val APP_OK_HTTP_CLIENT = "APP_OK_HTTP_CLIENT"
const val APP_WS = "APP_WS"

@Module
class NetworkModule {

    @Single
    @Named(BASIC_OK_HTTP_CLIENT)
    fun basicOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
        .build()

    @Single
    @Named(APP_WS)
    fun appWs(@Named(APP_OK_HTTP_CLIENT) okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BuildConfig.SERVER_ENDPOINT)
            .addConverterFactory(Json.asConverterFactory(contentType))
            .build()
    }
}
