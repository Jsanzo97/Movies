package jsanzo.movies.di.remote

import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val appRemoteModule = module {

    single { ChuckerInterceptor(androidContext()) }

    single(named(APP_OK_HTTP_CLIENT)) {
        val chuckerInterceptor = get<ChuckerInterceptor>()
        val basicOkHttpClient = get<OkHttpClient>(named(BASIC_OK_HTTP_CLIENT))

        basicOkHttpClient.newBuilder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(chuckerInterceptor)
            .build()
    }
}
