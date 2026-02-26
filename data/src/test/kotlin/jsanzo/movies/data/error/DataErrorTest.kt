package jsanzo.movies.data.error

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import jsanzo.movies.domain.error.AuthenticationError
import jsanzo.movies.domain.error.GenericError
import jsanzo.movies.domain.error.IOOperationError
import jsanzo.movies.domain.error.InvalidParametersError
import jsanzo.movies.domain.error.NotFoundError
import jsanzo.movies.domain.error.UnknownIOError
import org.junit.jupiter.api.Test

class DataErrorTest {

    @Test
    fun `WritingError maps to IOOperationError`() {
        WritingError.toMovieError() shouldBe IOOperationError
    }

    @Test
    fun `ReadingError maps to IOOperationError`() {
        ReadingError.toMovieError() shouldBe IOOperationError
    }

    @Test
    fun `UnknownError maps to UnknownIOError`() {
        UnknownError.toMovieError() shouldBe UnknownIOError
    }

    @Test
    fun `InvalidRequest maps to InvalidParametersError`() {
        InvalidRequest.toMovieError() shouldBe InvalidParametersError
    }

    @Test
    fun `InvalidCredentials maps to AuthenticationError`() {
        InvalidCredentials.toMovieError() shouldBe AuthenticationError
    }

    @Test
    fun `NotFound maps to NotFoundError`() {
        NotFound.toMovieError() shouldBe NotFoundError
    }

    @Test
    fun `UnrecognizedRemoteError maps to GenericError with message`() {
        val error = UnrecognizedRemoteError("something went wrong")
        val result = error.toMovieError()
        result.shouldBeInstanceOf<GenericError>()
        result.message shouldBe "something went wrong"
    }

    @Test
    fun `UnrecognizedRemoteError with empty message maps to GenericError with empty message`() {
        val error = UnrecognizedRemoteError()
        val result = error.toMovieError()
        result.shouldBeInstanceOf<GenericError>()
        result.message shouldBe ""
    }
}
