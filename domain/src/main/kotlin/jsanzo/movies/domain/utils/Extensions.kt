package jsanzo.movies.domain.utils

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some

fun <L, R> Either<L, R>.onSuccess(action: (R) -> Unit): Either<L, R> {
    if (this is Either.Right) action(value)
    return this
}

fun <L, R> Either<L, R>.onError(action: (L) -> Unit): Either<L, R> {
    if (this is Either.Left) action(value)
    return this
}

fun <T> Option<T>.onError(action: (T) -> Unit): Option<T> {
    if (this is Some) action(value)
    return this
}

fun <T> Option<T>.onSuccess(action: () -> Unit): Option<T> {
    if (this is None) action()
    return this
}
