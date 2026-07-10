package jsanzo.movies.domain.usecase

import arrow.core.Either
import jsanzo.movies.domain.error.MovieError
import jsanzo.movies.domain.repository.RemoteConfigRepository

class MustUpdateUseCase(
    private val remoteConfigRepository: RemoteConfigRepository,
) {

    suspend operator fun invoke(actualVersion: String): Either<MovieError, Boolean> {
        return remoteConfigRepository.getMinVersion()
            .map { mustUpdate(actualVersion, it.minVersion) }
    }

    private fun mustUpdate(actualVersion: String, minVersion: String): Boolean {
        val actual = actualVersion.split('.').map { it.toInt() }
        val min = minVersion.split('.').map { it.toInt() }
        return actual.zip(min).firstOrNull { (actual, min) -> actual != min }?.let { (actual, min) -> actual < min } == true
    }
}
