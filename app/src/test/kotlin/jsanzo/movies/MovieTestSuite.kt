package jsanzo.movies

import jsanzo.movies.ui.details.DetailsViewModelTest
import jsanzo.movies.ui.home.HomeViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite

@ExperimentalCoroutinesApi
@RunWith(Suite::class)
@Suite.SuiteClasses(
    HomeViewModelTest::class,
    DetailsViewModelTest::class,
)
class MovieTestSuite
