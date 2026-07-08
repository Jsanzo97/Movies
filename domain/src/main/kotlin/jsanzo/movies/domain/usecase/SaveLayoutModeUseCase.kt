package jsanzo.movies.domain.usecase

import jsanzo.movies.domain.model.DomainLayoutModePreference
import jsanzo.movies.domain.repository.DataStoreRepository

class SaveLayoutModeUseCase(private val repository: DataStoreRepository) {

    suspend operator fun invoke(mode: DomainLayoutModePreference): Unit = repository.saveLayoutMode(mode)
}
