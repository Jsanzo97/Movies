package jsanzo.movies.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import jsanzo.movies.domain.model.DomainLayoutModePreference
import jsanzo.movies.domain.repository.DataStoreRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SaveLayoutModeUseCaseTest {

    private val dataStoreRepository: DataStoreRepository = mockk()
    private val saveLayoutModeUseCase = SaveLayoutModeUseCase(dataStoreRepository)

    @Test
    fun `Given a layout mode, When invoke is called, Then repository saves the mode`() = runTest {
        coEvery { dataStoreRepository.saveLayoutMode(DomainLayoutModePreference.Grid2) } returns Unit

        saveLayoutModeUseCase(DomainLayoutModePreference.Grid2)

        coVerify(exactly = 1) { dataStoreRepository.saveLayoutMode(DomainLayoutModePreference.Grid2) }
        confirmVerified(dataStoreRepository)
    }
}
