package jsanzo.movies.remote.di

import jsanzo.movies.data.datastore.RemoteMoviesDatastore
import jsanzo.movies.remote.BuildConfig
import jsanzo.movies.remote.service.movies.MoviesService
import org.koin.core.annotation.ComponentScan
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
    ): RemoteMoviesDatastore = MoviesService(retrofit.create(), BuildConfig.SERVER_API_KEY)
}
