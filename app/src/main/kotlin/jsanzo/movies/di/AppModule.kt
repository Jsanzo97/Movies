package jsanzo.movies.di

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import com.google.firebase.analytics.FirebaseAnalytics
import jsanzo.movies.data.di.DataModule
import jsanzo.movies.database.di.DatabaseModule
import jsanzo.movies.datastore.di.DataStoreModule
import jsanzo.movies.domain.di.DomainModule
import jsanzo.movies.remote.di.NetworkModule
import jsanzo.movies.remote.di.RemoteModule
import jsanzo.movies.tracking.FirebaseTracker
import jsanzo.movies.tracking.MovieTracker
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module(
    includes = [
        DataModule::class,
        DomainModule::class,
        DatabaseModule::class,
        NetworkModule::class,
        RemoteModule::class,
        DataStoreModule::class,
    ],
)
@ComponentScan("jsanzo.movies")
class AppModule {

    @Single
    fun provideFirebaseTracker(androidContext: Application): MovieTracker = FirebaseTracker(
        analytics = FirebaseAnalytics.getInstance(androidContext),
    )

    @Single
    @Named("actualVersion")
    fun provideVersionName(context: Application): String {
        return try {
            val versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0),
                ).versionName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }
            versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }
}
