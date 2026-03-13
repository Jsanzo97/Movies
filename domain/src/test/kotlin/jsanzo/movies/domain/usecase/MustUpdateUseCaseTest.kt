package jsanzo.movies.domain.usecase

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import jsanzo.movies.domain.error.NotFoundError
import jsanzo.movies.domain.model.MinVersionDomainConfig
import jsanzo.movies.domain.repository.RemoteConfigRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class MustUpdateUseCaseTest {

    private val remoteConfigRepository: RemoteConfigRepository = mockk()
    private val useCase = MustUpdateUseCase(remoteConfigRepository)

    @Test
    fun `Given a repository error, When invoke is called, Then error is returned`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns NotFoundError.left()

        val result = useCase("1.0.0")

        result shouldBe NotFoundError.left()
        coVerify(exactly = 1) { remoteConfigRepository.getMinVersion() }
        confirmVerified(remoteConfigRepository)
    }

    @Test
    fun `Given equal versions, When invoke is called, Then mustUpdate is false`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.0.0").right()

        val result = useCase("1.0.0")

        result shouldBe false.right()
        coVerify(exactly = 1) { remoteConfigRepository.getMinVersion() }
        confirmVerified(remoteConfigRepository)
    }

    @Test
    fun `Given actual major is lower than min, When invoke is called, Then mustUpdate is true`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("2.0.0").right()

        val result = useCase("1.0.0")

        result shouldBe true.right()
        coVerify(exactly = 1) { remoteConfigRepository.getMinVersion() }
        confirmVerified(remoteConfigRepository)
    }

    @Test
    fun `Given actual major is higher than min, When invoke is called, Then mustUpdate is false`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.0.0").right()

        val result = useCase("2.0.0")

        result shouldBe false.right()
        coVerify(exactly = 1) { remoteConfigRepository.getMinVersion() }
        confirmVerified(remoteConfigRepository)
    }

    @Test
    fun `Given actual major is higher even if minor is lower, When invoke is called, Then mustUpdate is false`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.9.0").right()

        val result = useCase("2.0.0")

        result shouldBe false.right()
        coVerify(exactly = 1) { remoteConfigRepository.getMinVersion() }
        confirmVerified(remoteConfigRepository)
    }

    @Test
    fun `Given actual minor is lower than min, When invoke is called, Then mustUpdate is true`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.2.0").right()

        val result = useCase("1.1.0")

        result shouldBe true.right()
        coVerify(exactly = 1) { remoteConfigRepository.getMinVersion() }
        confirmVerified(remoteConfigRepository)
    }

    @Test
    fun `Given actual minor is higher than min, When invoke is called, Then mustUpdate is false`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.1.0").right()

        val result = useCase("1.2.0")

        result shouldBe false.right()
        coVerify(exactly = 1) { remoteConfigRepository.getMinVersion() }
        confirmVerified(remoteConfigRepository)
    }

    @Test
    fun `Given actual patch is lower than min, When invoke is called, Then mustUpdate is true`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.0.2").right()

        val result = useCase("1.0.1")

        result shouldBe true.right()
        coVerify(exactly = 1) { remoteConfigRepository.getMinVersion() }
        confirmVerified(remoteConfigRepository)
    }

    @Test
    fun `Given actual patch is higher than min, When invoke is called, Then mustUpdate is false`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.0.1").right()

        val result = useCase("1.0.2")

        result shouldBe false.right()
        coVerify(exactly = 1) { remoteConfigRepository.getMinVersion() }
        confirmVerified(remoteConfigRepository)
    }

    @Test
    fun `Given major and minor are equal and patch is higher, When invoke is called, Then mustUpdate is false`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.1.0").right()

        val result = useCase("1.1.5")

        result shouldBe false.right()
        coVerify(exactly = 1) { remoteConfigRepository.getMinVersion() }
        confirmVerified(remoteConfigRepository)
    }

    @Test
    fun `Given only patch differs and actual is lower, When invoke is called, Then mustUpdate is true`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.0.10").right()

        val result = useCase("1.0.9")

        result shouldBe true.right()
        coVerify(exactly = 1) { remoteConfigRepository.getMinVersion() }
        confirmVerified(remoteConfigRepository)
    }
}
