package jsanzo.movies.domain.error

sealed class MovieError

object InvalidParametersError: MovieError()
object AuthenticationError: MovieError()
object NotFoundError: MovieError()
object IOOperationError: MovieError()
object UnknownIOError: MovieError()
class GenericError(val message: String = ""): MovieError()
