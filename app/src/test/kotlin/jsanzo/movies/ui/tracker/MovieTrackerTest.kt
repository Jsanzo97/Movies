package jsanzo.movies.ui.tracker

import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.mockk
import io.mockk.verify
import jsanzo.movies.tracking.FirebaseTracker
import jsanzo.movies.tracking.MovieTracker
import org.junit.jupiter.api.Test

class MovieTrackerTest {

    private val mockedAnalytics: FirebaseAnalytics = mockk(relaxed = true)
    private val tracker: MovieTracker = FirebaseTracker(mockedAnalytics)

    @Test
    fun `trackHomeShown logs screen_view event with home screen name`() {
        tracker.trackHomeShown()
        verify { mockedAnalytics.logEvent("screen_view", any()) }
    }

    @Test
    fun `trackDetailsShown logs screen_view with details and movieId`() {
        tracker.trackDetailsShown(123)
        verify { mockedAnalytics.logEvent("screen_view", any()) }
    }

    @Test
    fun `trackMovieClicked logs movie_clicked event`() {
        tracker.trackMovieClicked(123, "Harry Potter")
        verify { mockedAnalytics.logEvent("movie_clicked", any()) }
    }

    @Test
    fun `trackErrorShown logs error_shown event`() {
        tracker.trackErrorShown("home", "error message")
        verify { mockedAnalytics.logEvent("error_shown", any()) }
    }

    @Test
    fun `trackPageLoaded logs page_loaded event`() {
        tracker.trackPageLoaded(1)
        verify { mockedAnalytics.logEvent("page_loaded", any()) }
    }

    @Test
    fun `trackSplashShown logs screen_view event with splash screen name`() {
        tracker.trackSplashShown()
        verify { mockedAnalytics.logEvent("screen_view", any()) }
    }

    @Test
    fun `trackForceUpdateShown logs force_update_shown event with current version`() {
        tracker.trackForceUpdateShown("1.0.0")
        verify { mockedAnalytics.logEvent("force_update_shown", any()) }
    }

    @Test
    fun `trackRemoteConfigError logs remote_config_error event`() {
        tracker.trackRemoteConfigError()
        verify { mockedAnalytics.logEvent("remote_config_error", any()) }
    }

    @Test
    fun `trackSearchPerformed logs search_performed event`() {
        tracker.trackSearchPerformed("harry potter", 10)
        verify { mockedAnalytics.logEvent("search_performed", any()) }
    }
}
