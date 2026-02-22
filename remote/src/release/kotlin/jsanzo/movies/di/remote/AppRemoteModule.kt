package jsanzo.movies.di.remote

import okhttp3.OkHttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
class AppRemoteModule {

    @Single
    @Named(APP_OK_HTTP_CLIENT)
    fun appOkHttpClient(@Named(BASIC_OK_HTTP_CLIENT) basicOkHttpClient: OkHttpClient): OkHttpClient = basicOkHttpClient
}
