package jsanzo.movies.di.data

import jsanzo.movies.data.datastore.LocalMoviesDatastore
import jsanzo.movies.data.datastore.RemoteMoviesDatastore
import jsanzo.movies.data.repository.MoviesDataRepository
import jsanzo.movies.database.storage.MoviesStorage
import jsanzo.movies.domain.repository.MoviesRepository
import jsanzo.movies.BuildConfig
import jsanzo.movies.di.remote.APP_OK_HTTP_CLIENT
import jsanzo.movies.di.remote.APP_WS
import jsanzo.movies.remote.service.movies.MoviesService
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create

val dataModule = module {

    /* MOVIES SERVICE */

    single<MoviesRepository> { MoviesDataRepository(get(), get(), Dispatchers.IO) }

    single<RemoteMoviesDatastore> {
        MoviesService(get<Retrofit>(named(APP_WS)).create(), BuildConfig.SERVER_API_KEY)
    }

    single<LocalMoviesDatastore> { MoviesStorage(get()) }

}
