package jsanzo.movies.data.di

import jsanzo.movies.data.datastore.LocalMoviesDatastore
import jsanzo.movies.data.datastore.RemoteMoviesDatastore
import jsanzo.movies.data.repository.MoviesDataRepository
import jsanzo.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DataModule {

    @Single
    fun moviesRepository(
        remote: RemoteMoviesDatastore,
        local: LocalMoviesDatastore,
    ): MoviesRepository = MoviesDataRepository(remote, local, Dispatchers.IO)
}
