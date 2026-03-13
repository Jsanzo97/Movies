package service

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jsanzo.movies.data.error.InvalidCredentials
import jsanzo.movies.data.error.InvalidRequest
import jsanzo.movies.data.error.NotFound
import jsanzo.movies.data.error.UnrecognizedRemoteError
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
    fun `Given a successful response, When executeNetworkRequest is called, Then body is returned`() = runTest {
        val result = networkHandler.executeNetworkRequest { Response.success("Success") }

        result shouldBe "Success".right()
    }

    @Test
    fun `Given a successful response with null body, When executeNetworkRequest is called, Then UnrecognizedRemoteError is returned`() = runTest {
        val result = networkHandler.executeNetworkRequest { Response.success<String>(null) }

        result shouldBe UnrecognizedRemoteError().left()
    }

    @Test
    fun `Given a 400 response, When executeNetworkRequest is called, Then InvalidRequest is returned`() = runTest {
        val result = networkHandler.executeNetworkRequest { Response.error<String>(400, "".toResponseBody(null)) }

        result shouldBe InvalidRequest.left()
    }

    @Test
    fun `Given a 401 response, When executeNetworkRequest is called, Then InvalidCredentials is returned`() = runTest {
        val result = networkHandler.executeNetworkRequest { Response.error<String>(401, "".toResponseBody(null)) }

        result shouldBe InvalidCredentials.left()
    }

    @Test
    fun `Given a 404 response, When executeNetworkRequest is called, Then NotFound is returned`() = runTest {
        val result = networkHandler.executeNetworkRequest { Response.error<String>(404, "".toResponseBody(null)) }

        result shouldBe NotFound.left()
    }

    @Test
    fun `Given a 500 response, When executeNetworkRequest is called, Then UnrecognizedRemoteError is returned`() = runTest {
        val result = networkHandler.executeNetworkRequest { Response.error<String>(500, "".toResponseBody(null)) }

        result shouldBe UnrecognizedRemoteError().left()
    }

    @Test
    fun `Given a 401 response with error body, When executeNetworkRequest is called, Then UnrecognizedRemoteError with message is returned`() = runTest {
        val errorJson = """{"status_message":"Invalid API key","success":false,"status_code":7}"""
        val result = networkHandler.executeNetworkRequest { Response.error<String>(401, errorJson.toResponseBody()) }

        result shouldBe UnrecognizedRemoteError("Invalid API key").left()
    }

    @Test
    fun `Given a response with null error body, When executeNetworkRequest is called, Then UnrecognizedRemoteError is returned`() = runTest {
        val response = mockk<Response<String>>()
        every { response.isSuccessful } returns false
        every { response.code() } returns 500
        every { response.errorBody() } returns null

        val result = networkHandler.executeNetworkRequest { response }

        result shouldBe UnrecognizedRemoteError().left()
        verify(exactly = 1) { response.isSuccessful }
        verify(exactly = 1) { response.errorBody() }
        verify(exactly = 1) { response.code() }
        confirmVerified(response)
    }

    @Test
    fun `Given an IOException, When executeNetworkRequest is called, Then UnrecognizedRemoteError with message is returned`() = runTest {
        val result = networkHandler.executeNetworkRequest<String> { throw IOException("Network failed") }

        result shouldBe UnrecognizedRemoteError("Network failed").left()
    }

    @Test
    fun `Given an IOException with null message, When executeNetworkRequest is called, Then UnrecognizedRemoteError with toString is returned`() = runTest {
        val exception = object : IOException() {
            override fun getLocalizedMessage(): String? = null
            override fun toString(): String = "IOException"
        }

        val result = networkHandler.executeNetworkRequest<String> { throw exception }

        result shouldBe UnrecognizedRemoteError("IOException").left()
    }

    @Test
    fun `Given an HttpException, When executeNetworkRequest is called, Then UnrecognizedRemoteError with message is returned`() = runTest {
        val httpException = HttpException(Response.error<Unit>(500, "".toResponseBody(null)))

        val result = networkHandler.executeNetworkRequest<String> { throw httpException }

        result shouldBe UnrecognizedRemoteError("HTTP 500 Response.error()").left()
    }

    @Test
    fun `Given an HttpException with null message, When executeNetworkRequest is called, Then UnrecognizedRemoteError with toString is returned`() = runTest {
        val httpException = object : HttpException(Response.error<Unit>(500, "".toResponseBody(null))) {
            override fun getLocalizedMessage(): String? = null
            override fun toString(): String = "HttpException"
        }

        val result = networkHandler.executeNetworkRequest<String> { throw httpException }

        result shouldBe UnrecognizedRemoteError("HttpException").left()
    }
}
