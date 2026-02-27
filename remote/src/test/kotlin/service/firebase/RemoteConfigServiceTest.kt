package service.firebase

import arrow.core.left
import arrow.core.right
import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jsanzo.movies.data.error.UnknownError
import jsanzo.movies.data.model.MinVersionDataConfig
import jsanzo.movies.remote.service.firebase.RemoteConfigService
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class RemoteConfigServiceTest {

    private val firebaseRemoteConfig: FirebaseRemoteConfig = mockk()
    private val json = Json { ignoreUnknownKeys = true }
    private val remoteConfigService = RemoteConfigService(firebaseRemoteConfig, json)

    private val validJson = """{"minVersion":"1.0.0"}"""

    @Test
    fun `getMinVersion returns MinVersionDataConfig on success`() = runTest {
        every { firebaseRemoteConfig.fetchAndActivate() } returns Tasks.forResult(true)
        every { firebaseRemoteConfig.getString("min_version") } returns validJson

        val result = remoteConfigService.getMinVersion()

        result shouldBe MinVersionDataConfig(minVersion = "1.0.0").right()
    }

    @Test
    fun `getMinVersion returns UnknownError when fetchAndActivate throws`() = runTest {
        every { firebaseRemoteConfig.fetchAndActivate() } returns Tasks.forException(Exception("Firebase error"))

        val result = remoteConfigService.getMinVersion()

        result shouldBe UnknownError.left()
    }

    @Test
    fun `getMinVersion returns UnknownError when json is malformed`() = runTest {
        every { firebaseRemoteConfig.fetchAndActivate() } returns Tasks.forResult(true)
        every { firebaseRemoteConfig.getString("min_version") } returns "invalid json"

        val result = remoteConfigService.getMinVersion()

        result shouldBe UnknownError.left()
    }

    @Test
    fun `getMinVersion returns UnknownError when json is empty`() = runTest {
        every { firebaseRemoteConfig.fetchAndActivate() } returns Tasks.forResult(true)
        every { firebaseRemoteConfig.getString("min_version") } returns ""

        val result = remoteConfigService.getMinVersion()

        result shouldBe UnknownError.left()
    }
}
