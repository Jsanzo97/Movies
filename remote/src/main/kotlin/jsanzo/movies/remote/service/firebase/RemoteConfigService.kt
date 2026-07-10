package jsanzo.movies.remote.service.firebase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import jsanzo.movies.data.datastore.RemoteConfigDataStore
import jsanzo.movies.data.error.DataError
import jsanzo.movies.data.error.UnknownError
import jsanzo.movies.data.model.MinVersionDataConfig
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class RemoteConfigService(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val json: Json,
) : RemoteConfigDataStore {

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override suspend fun getMinVersion(): Either<DataError, MinVersionDataConfig> {
        return try {
            firebaseRemoteConfig.fetchAndActivate().await()
            val jsonString = firebaseRemoteConfig.getString("min_version")
            json.decodeFromString<MinVersionDataConfig>(jsonString).right()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            UnknownError.left()
        }
    }
}
