package jsanzo.movies.data.datastore

import arrow.core.Either
import jsanzo.movies.data.error.DataError
import jsanzo.movies.data.model.MinVersionDataConfig

interface RemoteConfigDataStore {

    suspend fun getMinVersion(): Either<DataError, MinVersionDataConfig>
}
