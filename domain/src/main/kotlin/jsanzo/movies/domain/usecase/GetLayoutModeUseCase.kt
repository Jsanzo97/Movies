package jsanzo.movies.domain.usecase

import jsanzo.movies.domain.model.DomainLayoutModePreference
import jsanzo.movies.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow

class GetLayoutModeUseCase(private val repository: DataStoreRepository) {

    operator fun invoke(): Flow<DomainLayoutModePreference> = repository.getLayoutMode()
}
