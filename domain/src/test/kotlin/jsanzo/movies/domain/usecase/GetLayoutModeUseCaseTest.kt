package jsanzo.movies.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jsanzo.movies.domain.model.DomainLayoutModePreference
import jsanzo.movies.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetLayoutModeUseCaseTest {

    private val dataStoreRepository: DataStoreRepository = mockk()
    private val getLayoutModeUseCase = GetLayoutModeUseCase(dataStoreRepository)

    @Test
    fun `Given Grid2 is stored, When invoke is called, Then Grid2 is emitted`() = runTest {
        every { dataStoreRepository.getLayoutMode() } returns flowOf(DomainLayoutModePreference.Grid2)

        getLayoutModeUseCase().test {
            awaitItem() shouldBe DomainLayoutModePreference.Grid2
            awaitComplete()
        }

        verify(exactly = 1) { dataStoreRepository.getLayoutMode() }
        confirmVerified(dataStoreRepository)
    }
}
