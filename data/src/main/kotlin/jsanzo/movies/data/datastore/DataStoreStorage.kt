package jsanzo.movies.data.datastore

import jsanzo.movies.data.model.DataLayoutModePreference
import kotlinx.coroutines.flow.Flow

interface DataStoreStorage {

    fun getLayoutMode(): Flow<DataLayoutModePreference>
    suspend fun saveLayoutMode(mode: DataLayoutModePreference)
}
