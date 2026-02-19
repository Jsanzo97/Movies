package com.example.remote.service

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import com.example.common.EMPTY_STRING
import com.example.data.error.*
import com.example.remote.dto.response.ErrorResponse
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.HttpURLConnection

private val json = Json { ignoreUnknownKeys = true }

internal suspend fun <T : Any> executeNetworkRequest(f: suspend () -> Response<T>): Either<RemoteDataError, T> {
    return try {
        processResponse(f())
    } catch (error: Throwable) {
        UnrecognizedRemoteError(error.localizedMessage ?: error.toString()).left()
    }
}

internal suspend fun <T : Any> processResponse(response: Response<T>): Either<RemoteDataError, T> {
    return if (response.isSuccessful) {
        response.body()?.right() ?: UnrecognizedRemoteError().left()
    } else {
        val error = checkErrorResponse(response.errorBody()).fold(
            {
                when (response.code()) {
                    HttpURLConnection.HTTP_BAD_REQUEST -> InvalidRequest
                    HttpURLConnection.HTTP_UNAUTHORIZED -> InvalidCredentials
                    HttpURLConnection.HTTP_NOT_FOUND -> NotFound
                    else -> UnrecognizedRemoteError()
                }
            },
            {
                UnrecognizedRemoteError(it.statusMessage)
            }
        )

        error.left()
    }
}

private suspend fun checkErrorResponse(body: ResponseBody?): Option<ErrorResponse> = Either.catch {
    json.decodeFromString<ErrorResponse>(body?.string() ?: EMPTY_STRING)
}.getOrNone()
