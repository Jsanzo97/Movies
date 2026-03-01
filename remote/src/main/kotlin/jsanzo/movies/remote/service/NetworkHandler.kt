package jsanzo.movies.remote.service

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import jsanzo.movies.data.error.DataError
import jsanzo.movies.data.error.InvalidCredentials
import jsanzo.movies.data.error.InvalidRequest
import jsanzo.movies.data.error.NotFound
import jsanzo.movies.data.error.UnrecognizedRemoteError
import jsanzo.movies.remote.dto.response.ErrorResponse
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import org.koin.core.annotation.Single
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection

@Single
class NetworkHandler(private val json: Json) {

    suspend fun <T : Any> executeNetworkRequest(f: suspend () -> Response<T>): Either<DataError, T> {
        return try {
            processResponse(f())
        } catch (error: IOException) {
            UnrecognizedRemoteError(error.localizedMessage ?: error.toString()).left()
        } catch (error: HttpException) {
            UnrecognizedRemoteError(error.localizedMessage ?: error.toString()).left()
        }
    }

    private fun <T : Any> processResponse(response: Response<T>): Either<DataError, T> {
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
                },
            )

            error.left()
        }
    }

    private fun checkErrorResponse(body: ResponseBody?): Option<ErrorResponse> = Either.catch {
        json.decodeFromString<ErrorResponse>(body?.string() ?: "")
    }.getOrNone()
}
