package jsanzo.movies.domain.repository

import jsanzo.movies.domain.model.DomainLayoutModePreference
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    fun getLayoutMode(): Flow<DomainLayoutModePreference>
    suspend fun saveLayoutMode(mode: DomainLayoutModePreference)
}
