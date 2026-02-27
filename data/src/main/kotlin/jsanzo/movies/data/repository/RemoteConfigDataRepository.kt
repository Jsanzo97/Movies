package jsanzo.movies.data.repository

import arrow.core.Either
import jsanzo.movies.data.datastore.RemoteConfigDataStore
import jsanzo.movies.data.error.toMovieError
import jsanzo.movies.data.model.toDomain
import jsanzo.movies.domain.error.MovieError
import jsanzo.movies.domain.model.MinVersionDomainConfig
import jsanzo.movies.domain.repository.RemoteConfigRepository

class RemoteConfigDataRepository(
    private val remoteConfigDataStore: RemoteConfigDataStore,
) : RemoteConfigRepository {

    override suspend fun getMinVersion(): Either<MovieError, MinVersionDomainConfig> = remoteConfigDataStore.getMinVersion()
        .map { it.toDomain() }
        .mapLeft { it.toMovieError() }
}
