package jsanzo.movies

import android.app.Application
import jsanzo.movies.di.data.dataModule
import jsanzo.movies.di.details.detailsModule
import jsanzo.movies.di.home.homeModule
import jsanzo.movies.di.local.localModule
import jsanzo.movies.di.remote.appRemoteModule
import jsanzo.movies.di.remote.remoteModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MoviesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MoviesApplication)

            modules(
                listOf(
                    remoteModule,
                    appRemoteModule,
                    localModule,
                    dataModule,
                    homeModule,
                    detailsModule,
                ),
            )
        }
    }
}
