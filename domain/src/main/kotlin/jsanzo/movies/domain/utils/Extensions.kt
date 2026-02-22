package jsanzo.movies.domain.utils

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some

suspend fun <L, R> Either<L, R>.onSuccess(action: suspend (R) -> Unit): Either<L, R> {
    if (this is Either.Right) action(value)
    return this
}

suspend fun <L, R> Either<L, R>.onError(action: suspend (L) -> Unit): Either<L, R> {
    if (this is Either.Left) action(value)
    return this
}

suspend fun <T> Option<T>.onError(action: suspend (T) -> Unit): Option<T> {
    if (this is Some) action(value)
    return this
}

suspend fun <T> Option<T>.onSuccess(action: suspend () -> Unit): Option<T> {
    if (this is None) action()
    return this
}
