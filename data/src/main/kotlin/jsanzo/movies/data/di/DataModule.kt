package jsanzo.movies.data.di

import jsanzo.movies.data.datastore.DataStoreStorage
import jsanzo.movies.data.datastore.LocalMoviesDatastore
import jsanzo.movies.data.datastore.RemoteConfigDataStore
import jsanzo.movies.data.datastore.RemoteMoviesDatastore
import jsanzo.movies.data.repository.DataStoreDataRepository
import jsanzo.movies.data.repository.MoviesDataRepository
import jsanzo.movies.data.repository.RemoteConfigDataRepository
import jsanzo.movies.domain.repository.DataStoreRepository
import jsanzo.movies.domain.repository.MoviesRepository
import jsanzo.movies.domain.repository.RemoteConfigRepository
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

    @Single
    fun remoteConfigDataRepository(
        remoteConfigDataStore: RemoteConfigDataStore,
    ): RemoteConfigRepository = RemoteConfigDataRepository(remoteConfigDataStore)

    @Single
    fun dataStoreRepository(
        dataStoreStorage: DataStoreStorage,
    ): DataStoreRepository = DataStoreDataRepository(dataStoreStorage)
}
