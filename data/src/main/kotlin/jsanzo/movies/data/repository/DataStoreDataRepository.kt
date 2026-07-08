package jsanzo.movies.data.repository

import jsanzo.movies.data.datastore.DataStoreStorage
import jsanzo.movies.data.model.toData
import jsanzo.movies.data.model.toDomain
import jsanzo.movies.domain.model.DomainLayoutModePreference
import jsanzo.movies.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreDataRepository(
    private val dataStoreStorage: DataStoreStorage,
) : DataStoreRepository {

    override fun getLayoutMode(): Flow<DomainLayoutModePreference> = dataStoreStorage.getLayoutMode().map { it.toDomain() }

    override suspend fun saveLayoutMode(mode: DomainLayoutModePreference): Unit = dataStoreStorage.saveLayoutMode(mode.toData())
}
