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
    fun `Given home screen, When trackHomeShown is called, Then screen_view event is logged with home screen name`() {
        tracker.trackHomeShown()
        verify { mockedAnalytics.logEvent("screen_view", any()) }
    }

    @Test
    fun `Given a movie id, When trackDetailsShown is called, Then screen_view event is logged with details screen name and movie id`() {
        tracker.trackDetailsShown(123)
        verify { mockedAnalytics.logEvent("screen_view", any()) }
    }

    @Test
    fun `Given a movie id and title, When trackMovieClicked is called, Then movie_clicked event is logged`() {
        tracker.trackMovieClicked(123, "Harry Potter")
        verify { mockedAnalytics.logEvent("movie_clicked", any()) }
    }

    @Test
    fun `Given a screen and error, When trackErrorShown is called, Then error_shown event is logged`() {
        tracker.trackErrorShown("home", "error message")
        verify { mockedAnalytics.logEvent("error_shown", any()) }
    }

    @Test
    fun `Given a page number, When trackPageLoaded is called, Then page_loaded event is logged`() {
        tracker.trackPageLoaded(1)
        verify { mockedAnalytics.logEvent("page_loaded", any()) }
    }

    @Test
    fun `Given splash screen, When trackSplashShown is called, Then screen_view event is logged with splash screen name`() {
        tracker.trackSplashShown()
        verify { mockedAnalytics.logEvent("screen_view", any()) }
    }

    @Test
    fun `Given a current version, When trackForceUpdateShown is called, Then force_update_shown event is logged`() {
        tracker.trackForceUpdateShown("1.0.0")
        verify { mockedAnalytics.logEvent("force_update_shown", any()) }
    }

    @Test
    fun `Given error on remote config, When trackRemoteConfigError is called, Then remote_config_error event is logged`() {
        tracker.trackRemoteConfigError()
        verify { mockedAnalytics.logEvent("remote_config_error", any()) }
    }

    @Test
    fun `Given a query and results count, When trackSearchPerformed is called, Then search_performed event is logged`() {
        tracker.trackSearchPerformed("harry potter", 10)
        verify { mockedAnalytics.logEvent("search_performed", any()) }
    }

    @Test
    fun `Given a layout mode, When trackLayoutModeChanged is called, Then layout_mode_changed event is logged`() {
        tracker.trackLayoutModeChanged("Grid2")
        verify { mockedAnalytics.logEvent("layout_mode_changed", any()) }
    }
}
