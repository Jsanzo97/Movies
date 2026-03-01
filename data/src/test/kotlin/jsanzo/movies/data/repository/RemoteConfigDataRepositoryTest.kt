package jsanzo.movies.data.repository

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import jsanzo.movies.data.datastore.RemoteConfigDataStore
import jsanzo.movies.data.error.UnknownError
import jsanzo.movies.data.model.MinVersionDataConfig
import jsanzo.movies.data.model.toDomain
import jsanzo.movies.domain.error.UnknownIOError
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RemoteConfigDataRepositoryTest {

    private val remoteConfigDataStore: RemoteConfigDataStore = mockk()
    private val repository = RemoteConfigDataRepository(remoteConfigDataStore)

    private val minVersionDataConfig = MinVersionDataConfig(minVersion = "1.0.0")

    @Test
    fun `getMinVersion returns MinVersionDomainConfig on success`() = runTest {
        coEvery { remoteConfigDataStore.getMinVersion() } returns minVersionDataConfig.right()

        val result = repository.getMinVersion()

        result shouldBe minVersionDataConfig.toDomain().right()
    }

    @Test
    fun `getMinVersion returns MovieError on failure`() = runTest {
        coEvery { remoteConfigDataStore.getMinVersion() } returns UnknownError.left()

        val result = repository.getMinVersion()

        result shouldBe UnknownIOError.left()
    }
}
