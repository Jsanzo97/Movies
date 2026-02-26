package jsanzo.movies.domain.utils

import arrow.core.Either
import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ExtensionsTests {

    @Test
    fun `Either onSuccess executes action when Right`() = runTest {
        var executed = false
        val either: Either<String, Int> = 1.right()

        either.onSuccess { executed = true }

        executed shouldBe true
    }

    @Test
    fun `Either onSuccess does not execute action when Left`() = runTest {
        var executed = false
        val either: Either<String, Int> = "error".left()

        either.onSuccess { executed = true }

        executed shouldBe false
    }

    @Test
    fun `Either onSuccess returns original Either`() = runTest {
        val either: Either<String, Int> = 1.right()

        val result = either.onSuccess { }

        result shouldBe either
    }

    @Test
    fun `Either onError executes action when Left`() = runTest {
        var executed = false
        val either: Either<String, Int> = "error".left()

        either.onError { executed = true }

        executed shouldBe true
    }

    @Test
    fun `Either onError does not execute action when Right`() = runTest {
        var executed = false
        val either: Either<String, Int> = 1.right()

        either.onError { executed = true }

        executed shouldBe false
    }

    @Test
    fun `Either onError returns original Either`() = runTest {
        val either: Either<String, Int> = "error".left()

        val result = either.onError { }

        result shouldBe either
    }

    @Test
    fun `Option onError executes action when Some`() = runTest {
        var executed = false
        val option = 1.some()

        option.onError { executed = true }

        executed shouldBe true
    }

    @Test
    fun `Option onError does not execute action when None`() = runTest {
        var executed = false
        val option = None

        option.onError { executed = true }

        executed shouldBe false
    }

    @Test
    fun `Option onError returns original Option`() = runTest {
        val option = 1.some()

        val result = option.onError { }

        result shouldBe option
    }

    @Test
    fun `Option onSuccess executes action when None`() = runTest {
        var executed = false
        val option = None

        option.onSuccess { executed = true }

        executed shouldBe true
    }

    @Test
    fun `Option onSuccess does not execute action when Some`() = runTest {
        var executed = false
        val option = 1.some()

        option.onSuccess { executed = true }

        executed shouldBe false
    }

    @Test
    fun `Option onSuccess returns original Option`() = runTest {
        val option = None

        val result = option.onSuccess { }

        result shouldBe option
    }
}
