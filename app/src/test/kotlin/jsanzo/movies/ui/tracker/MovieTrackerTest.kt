package jsanzo.movies.ui.tracker

import io.mockk.mockk
import io.mockk.verify
import jsanzo.movies.tracking.MovieTracker
import org.junit.jupiter.api.Test

class MovieTrackerTest {

    private val tracker: MovieTracker = mockk(relaxed = true)

    @Test
    fun `trackHomeShown is called`() {
        tracker.trackHomeShown()
        verify(exactly = 1) { tracker.trackHomeShown() }
    }

    @Test
    fun `trackDetailsShown is called with correct movieId`() {
        val movieId = 123
        tracker.trackDetailsShown(movieId)
        verify(exactly = 1) { tracker.trackDetailsShown(movieId) }
    }

    @Test
    fun `trackMovieClicked is called with correct parameters`() {
        val movieId = 123
        val movieTitle = "Harry Potter"
        tracker.trackMovieClicked(movieId, movieTitle)
        verify(exactly = 1) { tracker.trackMovieClicked(movieId, movieTitle) }
    }

    @Test
    fun `trackErrorShown is called with correct parameters`() {
        val screen = "home"
        val error = "error message"
        tracker.trackErrorShown(screen, error)
        verify(exactly = 1) { tracker.trackErrorShown(screen, error) }
    }

    @Test
    fun `trackPageLoaded is called with correct page`() {
        val page = 1
        tracker.trackPageLoaded(page)
        verify(exactly = 1) { tracker.trackPageLoaded(page) }
    }
}
