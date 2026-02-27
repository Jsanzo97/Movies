package usecase

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import jsanzo.movies.domain.error.MovieError
import jsanzo.movies.domain.model.MinVersionDomainConfig
import jsanzo.movies.domain.repository.RemoteConfigRepository
import jsanzo.movies.domain.usecase.MustUpdateUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class MustUpdateUseCaseTest {

    private val remoteConfigRepository: RemoteConfigRepository = mockk()
    private val useCase = MustUpdateUseCase(remoteConfigRepository)

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        val error = mockk<MovieError>()
        coEvery { remoteConfigRepository.getMinVersion() } returns error.left()

        val result = useCase("1.0.0")

        result shouldBe error.left()
    }

    @Test
    fun `mustUpdate returns false when versions are equal`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.0.0").right()

        useCase("1.0.0").getOrNull() shouldBe false
    }

    @Test
    fun `mustUpdate returns true when actual major is lower`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("2.0.0").right()

        useCase("1.0.0").getOrNull() shouldBe true
    }

    @Test
    fun `mustUpdate returns false when actual major is higher`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.0.0").right()

        useCase("2.0.0").getOrNull() shouldBe false
    }

    @Test
    fun `mustUpdate returns true when actual minor is lower`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.2.0").right()

        useCase("1.1.0").getOrNull() shouldBe true
    }

    @Test
    fun `mustUpdate returns false when actual minor is higher`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.1.0").right()

        useCase("1.2.0").getOrNull() shouldBe false
    }

    @Test
    fun `mustUpdate returns false when major is higher even if minor is lower`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.9.0").right()

        useCase("2.0.0").getOrNull() shouldBe false
    }

    @Test
    fun `mustUpdate returns true when actual patch is lower`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.0.2").right()

        useCase("1.0.1").getOrNull() shouldBe true
    }

    @Test
    fun `mustUpdate returns false when actual patch is higher`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.0.1").right()

        useCase("1.0.2").getOrNull() shouldBe false
    }

    @Test
    fun `mustUpdate returns false when major and minor are equal and patch is higher`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.1.0").right()

        useCase("1.1.5").getOrNull() shouldBe false
    }

    @Test
    fun `mustUpdate returns true when only patch differs and actual is lower`() = runTest {
        coEvery { remoteConfigRepository.getMinVersion() } returns MinVersionDomainConfig("1.0.10").right()

        useCase("1.0.9").getOrNull() shouldBe true
    }
}
