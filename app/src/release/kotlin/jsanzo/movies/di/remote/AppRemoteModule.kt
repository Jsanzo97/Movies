package jsanzo.movies.di.remote

import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appRemoteModule = module {
    single(named(APP_OK_HTTP_CLIENT)) {
        get<OkHttpClient>(named(BASIC_OK_HTTP_CLIENT))
    }
}