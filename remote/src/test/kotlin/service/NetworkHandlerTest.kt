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
import jsanzo.movies.remote.service.NetworkHandler
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NetworkHandlerTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val networkHandler = NetworkHandler(json)

    @Test
    fun `executeNetworkRequest returns Right on success`() = runTest {
        val response = Response.success("Success")
        val result = networkHandler.executeNetworkRequest { response }
        result shouldBe "Success".right()
    }

    @Test
    fun `executeNetworkRequest returns UnrecognizedRemoteError on IOException`() = runTest {
        val result = networkHandler.executeNetworkRequest<String> { throw IOException("Network failed") }
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
        val result = networkHandler.executeNetworkRequest<String> { throw exception }
        result shouldBe UnrecognizedRemoteError("IOException").left()
    }

    @Test
    fun `executeNetworkRequest returns UnrecognizedRemoteError on HttpException`() = runTest {
        val httpException = HttpException(Response.error<Unit>(500, "".toResponseBody(null)))
        val result = networkHandler.executeNetworkRequest<String> { throw httpException }
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
        val result = networkHandler.executeNetworkRequest<String> { throw httpException }
        result shouldBe UnrecognizedRemoteError("HttpException").left()
    }

    @Test
    fun `executeNetworkRequest returns Right on successful response with body`() = runTest {
        val response = Response.success("Success")
        val result = networkHandler.executeNetworkRequest { response }
        result shouldBe "Success".right()
    }

    @Test
    fun `executeNetworkRequest returns UnrecognizedRemoteError on successful response with null body`() = runTest {
        val response = Response.success<String>(null)
        val result = networkHandler.executeNetworkRequest { response }
        result shouldBe UnrecognizedRemoteError().left()
    }

    @Test
    fun `executeNetworkRequest returns InvalidRequest on 400 error`() = runTest {
        val response = Response.error<String>(400, "".toResponseBody(null))
        val result = networkHandler.executeNetworkRequest { response }
        result shouldBe InvalidRequest.left()
    }

    @Test
    fun `executeNetworkRequest returns InvalidCredentials on 401 error`() = runTest {
        val response = Response.error<String>(401, "".toResponseBody(null))
        val result = networkHandler.executeNetworkRequest { response }
        result shouldBe InvalidCredentials.left()
    }

    @Test
    fun `executeNetworkRequest returns NotFound on 404 error`() = runTest {
        val response = Response.error<String>(404, "".toResponseBody(null))
        val result = networkHandler.executeNetworkRequest { response }
        result shouldBe NotFound.left()
    }

    @Test
    fun `executeNetworkRequest returns UnrecognizedRemoteError on other error codes`() = runTest {
        val response = Response.error<String>(500, "".toResponseBody(null))
        val result = networkHandler.executeNetworkRequest { response }
        result shouldBe UnrecognizedRemoteError().left()
    }

    @Test
    fun `executeNetworkRequest returns UnrecognizedRemoteError with message from error body`() = runTest {
        val errorJson = """{"status_message":"Invalid API key","success":false,"status_code":7}"""
        val response = Response.error<String>(401, errorJson.toResponseBody())
        val result = networkHandler.executeNetworkRequest { response }
        val error = UnrecognizedRemoteError("Invalid API key")

        result shouldBe error.left()
        result.shouldBeInstanceOf<Either.Left<UnrecognizedRemoteError>>()
        result.leftOrNull()?.message shouldBe "Invalid API key"
    }

    @Test
    fun `executeNetworkRequest returns UnrecognizedRemoteError when errorBody is null`() = runTest {
        val response = mockk<Response<String>>()
        every { response.isSuccessful } returns false
        every { response.code() } returns 500
        every { response.errorBody() } returns null
        val result = networkHandler.executeNetworkRequest { response }
        result shouldBe UnrecognizedRemoteError().left()
    }

    @Test
    fun `ErrorResponse properties are accessible`() {
        val errorResponse = ErrorResponse(statusMessage = "Error", statusCode = 500)
        errorResponse.statusMessage shouldBe "Error"
        errorResponse.statusCode shouldBe 500
    }
}
