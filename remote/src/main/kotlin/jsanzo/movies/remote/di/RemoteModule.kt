package jsanzo.movies.remote.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import jsanzo.movies.data.datastore.RemoteMoviesDatastore
import jsanzo.movies.remote.BuildConfig
import jsanzo.movies.remote.service.NetworkHandler
import jsanzo.movies.remote.service.movies.MoviesService
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import retrofit2.Retrofit
import retrofit2.create

@Module
class RemoteModule {

    @Single
    fun remoteMoviesDatastore(
        @Named(APP_WS) retrofit: Retrofit,
        networkHandler: NetworkHandler,
    ): RemoteMoviesDatastore = MoviesService(retrofit.create(), networkHandler, BuildConfig.SERVER_API_KEY)

    @Single
    fun firebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        return remoteConfig
    }
}
