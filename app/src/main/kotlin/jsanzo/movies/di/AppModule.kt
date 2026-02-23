package jsanzo.movies.di

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import jsanzo.movies.data.di.DataModule
import jsanzo.movies.database.di.DatabaseModule
import jsanzo.movies.domain.di.DomainModule
import jsanzo.movies.remote.di.NetworkModule
import jsanzo.movies.remote.di.RemoteModule
import jsanzo.movies.tracking.FirebaseTracker
import jsanzo.movies.tracking.MovieTracker
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(
    includes = [
        DataModule::class,
        DomainModule::class,
        DatabaseModule::class,
        NetworkModule::class,
        RemoteModule::class,
    ],
)
@ComponentScan("jsanzo.movies")
class AppModule {

    @Single
    fun provideFirebaseTracker(androidContext: Application): MovieTracker = FirebaseTracker(
        analytics = FirebaseAnalytics.getInstance(androidContext),
    )
}
