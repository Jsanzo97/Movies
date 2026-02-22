package jsanzo.movies.data.error

import jsanzo.movies.domain.error.AuthenticationError
import jsanzo.movies.domain.error.GenericError
import jsanzo.movies.domain.error.IOOperationError
import jsanzo.movies.domain.error.InvalidParametersError
import jsanzo.movies.domain.error.NotFoundError
import jsanzo.movies.domain.error.UnknownIOError

sealed class DataError

object WritingError : DataError()
object ReadingError : DataError()
object UnknownError : DataError()

object InvalidRequest : DataError()
object InvalidCredentials : DataError()
object NotFound : DataError()
data class UnrecognizedRemoteError(val message: String = "") : DataError()

fun DataError.toMovieError() = when (this) {
    WritingError, ReadingError -> IOOperationError
    UnknownError -> UnknownIOError
    InvalidRequest -> InvalidParametersError
    InvalidCredentials -> AuthenticationError
    NotFound -> NotFoundError
    is UnrecognizedRemoteError -> GenericError(message)
}
