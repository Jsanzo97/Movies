package service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import jsanzo.movies.data.error.InvalidCredentials
import jsanzo.movies.data.error.InvalidRequest
import jsanzo.movies.data.error.NotFound
import jsanzo.movies.data.error.UnrecognizedRemoteError
import jsanzo.movies.remote.dto.response.ErrorResponse
import jsanzo.movies.remote.service.executeNetworkRequest
import jsanzo.movies.remote.service.processResponse
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ResponseProcessesTest {

    @Test
    fun `executeNetworkRequest returns Right on success`() = runTest {
        val response = Response.success("Success")
        val result = executeNetworkRequest { response }
        result shouldBe "Success".right()
    }

    @Test
    fun `executeNetworkRequest returns UnrecognizedRemoteError on IOException`() = runTest {
        val result = executeNetworkRequest<String> { throw IOException("Network failed") }
        val error = UnrecognizedRemoteError("Network failed")

        result shouldBe error.left()
        result.shouldBeInstanceOf<Either.Left<UnrecognizedRemoteError>>()
        result.leftOrNull()?.message shouldBe "Network failed"
    }

    @Test
    fun `executeNetworkRequest returns UnrecognizedRemoteError on IOException with null message`() = runTest {
        val exception = object : IOException() {
            override fun getLocalizedMessage(): String? = null
            override fun toString(): String = "IOException"
        }
        val result = executeNetworkRequest<String> { throw exception }
        result shouldBe UnrecognizedRemoteError("IOException").left()
    }

    @Test
    fun `executeNetworkRequest returns UnrecognizedRemoteError on HttpException`() = runTest {
        val httpException = HttpException(Response.error<Unit>(500, "".toResponseBody(null)))
        val result = executeNetworkRequest<String> { throw httpException }
        val error = UnrecognizedRemoteError("HTTP 500 Response.error()")

        result shouldBe error.left()
        result.shouldBeInstanceOf<Either.Left<UnrecognizedRemoteError>>()
        result.leftOrNull()?.message shouldBe "HTTP 500 Response.error()"
    }

    @Test
    fun `executeNetworkRequest returns UnrecognizedRemoteError on HttpException with null message`() = runTest {
        val httpException = object : HttpException(Response.error<Unit>(500, "".toResponseBody(null))) {
            override fun getLocalizedMessage(): String? = null
            override fun toString(): String = "HttpException"
        }
        val result = executeNetworkRequest<String> { throw httpException }
        result shouldBe UnrecognizedRemoteError("HttpException").left()
    }

    @Test
    fun `processResponse returns Right on successful response with body`() {
        val response = Response.success("Success")
        val result = processResponse(response)
        result shouldBe "Success".right()
    }

    @Test
    fun `processResponse returns UnrecognizedRemoteError on successful response with null body`() {
        val response = Response.success<String>(null)
        val result = processResponse(response)
        result shouldBe UnrecognizedRemoteError().left()
    }

    @Test
    fun `processResponse returns InvalidRequest on 400 error`() {
        val response = Response.error<Unit>(400, "".toResponseBody(null))
        val result = processResponse(response)
        result shouldBe InvalidRequest.left()
    }

    @Test
    fun `processResponse returns InvalidCredentials on 401 error`() {
        val response = Response.error<Unit>(401, "".toResponseBody(null))
        val result = processResponse(response)
        result shouldBe InvalidCredentials.left()
    }

    @Test
    fun `processResponse returns NotFound on 404 error`() {
        val response = Response.error<Unit>(404, "".toResponseBody(null))
        val result = processResponse(response)
        result shouldBe NotFound.left()
    }

    @Test
    fun `processResponse returns UnrecognizedRemoteError on other error codes`() {
        val response = Response.error<Unit>(500, "".toResponseBody(null))
        val result = processResponse(response)
        result shouldBe UnrecognizedRemoteError().left()
    }

    @Test
    fun `processResponse returns UnrecognizedRemoteError with message from error body`() {
        val errorJson = """{"status_message":"Invalid API key","success":false,"status_code":7}"""
        val response = Response.error<Unit>(401, errorJson.toResponseBody())
        val result = processResponse(response)
        val error = UnrecognizedRemoteError("Invalid API key")

        result shouldBe error.left()
        result.shouldBeInstanceOf<Either.Left<UnrecognizedRemoteError>>()
        result.leftOrNull()?.message shouldBe "Invalid API key"
    }

    @Test
    fun `processResponse returns UnrecognizedRemoteError when errorBody is null`() {
        val response = mockk<Response<String>>()
        every { response.isSuccessful } returns false
        every { response.code() } returns 500
        every { response.errorBody() } returns null
        val result = processResponse(response)
        result shouldBe UnrecognizedRemoteError().left()
    }

    @Test
    fun `ErrorResponse properties are accessible`() {
        val errorResponse = ErrorResponse(statusMessage = "Error", statusCode = 500)
        errorResponse.statusMessage shouldBe "Error"
        errorResponse.statusCode shouldBe 500
    }
}
