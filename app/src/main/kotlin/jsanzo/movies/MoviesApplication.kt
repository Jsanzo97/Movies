package jsanzo.movies

import android.app.Application
import android.os.StrictMode
import jsanzo.movies.BuildConfig
import jsanzo.movies.di.AppModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class MoviesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupStrictMode()
        startKoin {
            androidLogger()
            androidContext(this@MoviesApplication)
            modules(AppModule().module)
        }
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .detectCustomSlowCalls()
                    .penaltyLog()
                    .build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
        }
    }
}
