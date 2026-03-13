package jsanzo.movies.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jsanzo.movies.data.datastore.DataStoreStorage
import jsanzo.movies.data.model.DataLayoutModePreference
import jsanzo.movies.data.model.toDomain
import jsanzo.movies.domain.model.DomainLayoutModePreference
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DataStoreDataRepositoryTest {

    private val dataStoreStorage: DataStoreStorage = mockk()
    private val repository = DataStoreDataRepository(dataStoreStorage)

    @Test
    fun `Given Grid2 is stored, When getLayoutMode is called, Then Grid2 domain preference is emitted`() = runTest {
        every { dataStoreStorage.getLayoutMode() } returns flowOf(DataLayoutModePreference.Grid2)

        repository.getLayoutMode().test {
            awaitItem() shouldBe DataLayoutModePreference.Grid2.toDomain()
            awaitComplete()
        }

        verify(exactly = 1) { dataStoreStorage.getLayoutMode() }
        confirmVerified(dataStoreStorage)
    }

    @Test
    fun `Given Grid3 is stored, When getLayoutMode is called, Then Grid3 domain preference is emitted`() = runTest {
        every { dataStoreStorage.getLayoutMode() } returns flowOf(DataLayoutModePreference.Grid3)

        repository.getLayoutMode().test {
            awaitItem() shouldBe DataLayoutModePreference.Grid3.toDomain()
            awaitComplete()
        }

        verify(exactly = 1) { dataStoreStorage.getLayoutMode() }
        confirmVerified(dataStoreStorage)
    }

    @Test
    fun `Given Grid4 is stored, When getLayoutMode is called, Then Grid4 domain preference is emitted`() = runTest {
        every { dataStoreStorage.getLayoutMode() } returns flowOf(DataLayoutModePreference.Grid4)

        repository.getLayoutMode().test {
            awaitItem() shouldBe DataLayoutModePreference.Grid4.toDomain()
            awaitComplete()
        }

        verify(exactly = 1) { dataStoreStorage.getLayoutMode() }
        confirmVerified(dataStoreStorage)
    }

    @Test
    fun `Given Grid2 domain preference, When saveLayoutMode is called, Then Grid2 data preference is persisted`() = runTest {
        coEvery { dataStoreStorage.saveLayoutMode(DataLayoutModePreference.Grid2) } just runs

        repository.saveLayoutMode(DomainLayoutModePreference.Grid2)

        coVerify(exactly = 1) { dataStoreStorage.saveLayoutMode(DataLayoutModePreference.Grid2) }
        confirmVerified(dataStoreStorage)
    }

    @Test
    fun `Given Grid3 domain preference, When saveLayoutMode is called, Then Grid3 data preference is persisted`() = runTest {
        coEvery { dataStoreStorage.saveLayoutMode(DataLayoutModePreference.Grid3) } just runs

        repository.saveLayoutMode(DomainLayoutModePreference.Grid3)

        coVerify(exactly = 1) { dataStoreStorage.saveLayoutMode(DataLayoutModePreference.Grid3) }
        confirmVerified(dataStoreStorage)
    }

    @Test
    fun `Given Grid4 domain preference, When saveLayoutMode is called, Then Grid4 data preference is persisted`() = runTest {
        coEvery { dataStoreStorage.saveLayoutMode(DataLayoutModePreference.Grid4) } just runs

        repository.saveLayoutMode(DomainLayoutModePreference.Grid4)

        coVerify(exactly = 1) { dataStoreStorage.saveLayoutMode(DataLayoutModePreference.Grid4) }
        confirmVerified(dataStoreStorage)
    }
}
