package jsanzo.movies.tracking

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

interface MovieTracker {
    fun trackHomeShown()
    fun trackDetailsShown(movieId: Int)
    fun trackMovieClicked(movieId: Int, movieTitle: String)
    fun trackErrorShown(screen: String, error: String)
    fun trackPageLoaded(page: Int)
    fun trackSplashShown()
    fun trackForceUpdateShown(currentVersion: String)
    fun trackRemoteConfigError()
    fun trackSearchPerformed(query: String, resultsCount: Int)
    fun trackLayoutModeChanged(mode: String)
}

internal class FirebaseTracker(
    private val analytics: FirebaseAnalytics,
) : MovieTracker {

    override fun trackHomeShown() {
        analytics.logEvent("screen_view") {
            param("screen_name", "home")
        }
    }

    override fun trackDetailsShown(movieId: Int) {
        analytics.logEvent("screen_view") {
            param("screen_name", "details")
            param("movie_id", movieId.toLong())
        }
    }

    override fun trackMovieClicked(movieId: Int, movieTitle: String) {
        analytics.logEvent("movie_clicked") {
            param("movie_id", movieId.toLong())
            param("movie_title", movieTitle)
        }
    }

    override fun trackErrorShown(screen: String, error: String) {
        analytics.logEvent("error_shown") {
            param("screen", screen)
            param("error", error)
        }
    }

    override fun trackPageLoaded(page: Int) {
        analytics.logEvent("page_loaded") {
            param("page", page.toLong())
        }
    }

    override fun trackSplashShown() {
        analytics.logEvent("screen_view") {
            param("screen_name", "splash")
        }
    }

    override fun trackForceUpdateShown(currentVersion: String) {
        analytics.logEvent("force_update_shown") {
            param("current_version", currentVersion)
        }
    }

    override fun trackRemoteConfigError() {
        analytics.logEvent("remote_config_error") {}
    }

    override fun trackSearchPerformed(query: String, resultsCount: Int) {
        analytics.logEvent("search_performed") {
            param("query", query)
            param("results_count", resultsCount.toLong())
        }
    }

    override fun trackLayoutModeChanged(mode: String) {
        analytics.logEvent("layout_mode_changed") {
            param("mode", mode)
        }
    }
}
