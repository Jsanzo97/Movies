package jsanzo.movies.domain.repository

import arrow.core.Either
import jsanzo.movies.domain.error.MovieError
import jsanzo.movies.domain.model.MinVersionDomainConfig

interface RemoteConfigRepository {

    suspend fun getMinVersion(): Either<MovieError, MinVersionDomainConfig>
}
