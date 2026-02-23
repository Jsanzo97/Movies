package jsanzo.movies.tracking

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

interface MovieTracker {
    fun trackHomeShown()
    fun trackDetailsShown(movieId: Int)
    fun trackMovieClicked(movieId: Int, movieTitle: String)
    fun trackErrorShown(screen: String, error: String)
    fun trackPageLoaded(page: Int)
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
}
