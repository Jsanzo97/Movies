package jsanzo.movies.data.error

import jsanzo.movies.domain.error.AuthenticationError
import jsanzo.movies.domain.error.GenericError
import jsanzo.movies.domain.error.InvalidParametersError
import jsanzo.movies.domain.error.NotFoundError

sealed class RemoteDataError

object InvalidRequest: RemoteDataError()
object InvalidCredentials: RemoteDataError()
object NotFound: RemoteDataError()
data class UnrecognizedRemoteError(val message: String = "") : RemoteDataError()

fun RemoteDataError.toMovieError() = when (this) {
    InvalidRequest -> InvalidParametersError
    InvalidCredentials -> AuthenticationError
    NotFound -> NotFoundError
    is UnrecognizedRemoteError -> GenericError(message)
}