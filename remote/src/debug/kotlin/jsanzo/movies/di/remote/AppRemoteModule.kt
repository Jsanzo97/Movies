package jsanzo.movies.di.remote

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import jsanzo.movies.remote.di.APP_OK_HTTP_CLIENT
import jsanzo.movies.remote.di.BASIC_OK_HTTP_CLIENT
import jsanzo.movies.remote.di.TIMEOUT
import okhttp3.OkHttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.util.concurrent.TimeUnit

@Module
class AppRemoteModule {

    @Single
    fun chuckerInterceptor(context: Context): ChuckerInterceptor = ChuckerInterceptor(context)

    @Single
    @Named(APP_OK_HTTP_CLIENT)
    fun appOkHttpClient(
        chuckerInterceptor: ChuckerInterceptor,
        @Named(BASIC_OK_HTTP_CLIENT) basicOkHttpClient: OkHttpClient,
    ): OkHttpClient = basicOkHttpClient.newBuilder()
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(chuckerInterceptor)
        .build()
}
