package jsanzo.movies.data.error

import jsanzo.movies.domain.error.IOOperationError
import jsanzo.movies.domain.error.UnknownIOError

sealed class LocalDataError

object WritingError : LocalDataError()
object ReadingError : LocalDataError()
object UnknownError : LocalDataError()

fun LocalDataError.toMovieError() = when (this) {
    WritingError -> IOOperationError
    ReadingError -> IOOperationError
    UnknownError -> UnknownIOError
}
