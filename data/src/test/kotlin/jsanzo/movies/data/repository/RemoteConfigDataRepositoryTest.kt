package jsanzo.movies.data.repository

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import jsanzo.movies.data.datastore.RemoteConfigDataStore
import jsanzo.movies.data.error.UnknownError
import jsanzo.movies.data.error.toMovieError
import jsanzo.movies.data.model.MinVersionDataConfig
import jsanzo.movies.data.model.toDomain
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RemoteConfigDataRepositoryTest {

    private val remoteConfigDataStore: RemoteConfigDataStore = mockk()
    private val repository = RemoteConfigDataRepository(remoteConfigDataStore)

    private val minVersionDataConfig = MinVersionDataConfig(minVersion = "1.0.0")

    @Test
    fun `Given a valid remote config, When getMinVersion is called, Then domain config is returned`() = runTest {
        coEvery { remoteConfigDataStore.getMinVersion() } returns minVersionDataConfig.right()

        val result = repository.getMinVersion()

        result shouldBe minVersionDataConfig.toDomain().right()
        coVerify(exactly = 1) { remoteConfigDataStore.getMinVersion() }
        confirmVerified(remoteConfigDataStore)
    }

    @Test
    fun `Given a remote config error, When getMinVersion is called, Then error is returned`() = runTest {
        coEvery { remoteConfigDataStore.getMinVersion() } returns UnknownError.left()

        val result = repository.getMinVersion()

        result shouldBe UnknownError.toMovieError().left()
        coVerify(exactly = 1) { remoteConfigDataStore.getMinVersion() }
        confirmVerified(remoteConfigDataStore)
    }
}
