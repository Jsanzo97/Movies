package jsanzo.movies.tracking

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import jsanzo.movies.BuildConfig

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

    private fun logEvent(event: String, params: Map<String, Any> = emptyMap()) {
        if (BuildConfig.DEBUG) {
            val paramsLog = if (params.isEmpty()) "" else " $params"
            Log.d("MovieTracker", "event=$event$paramsLog")
        }
        analytics.logEvent(event) {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Long -> param(key, value)
                }
            }
        }
    }

    override fun trackHomeShown() {
        logEvent("screen_view", mapOf("screen_name" to "home"))
    }

    override fun trackDetailsShown(movieId: Int) {
        logEvent("screen_view", mapOf("screen_name" to "details", "movie_id" to movieId.toLong()))
    }

    override fun trackMovieClicked(movieId: Int, movieTitle: String) {
        logEvent("movie_clicked", mapOf("movie_id" to movieId.toLong(), "movie_title" to movieTitle))
    }

    override fun trackErrorShown(screen: String, error: String) {
        logEvent("error_shown", mapOf("screen" to screen, "error" to error))
    }

    override fun trackPageLoaded(page: Int) {
        logEvent("page_loaded", mapOf("page" to page.toLong()))
    }

    override fun trackSplashShown() {
        logEvent("screen_view", mapOf("screen_name" to "splash"))
    }

    override fun trackForceUpdateShown(currentVersion: String) {
        logEvent("force_update_shown", mapOf("current_version" to currentVersion))
    }

    override fun trackRemoteConfigError() {
        logEvent("remote_config_error")
    }

    override fun trackSearchPerformed(query: String, resultsCount: Int) {
        logEvent("search_performed", mapOf("query" to query, "results_count" to resultsCount.toLong()))
    }

    override fun trackLayoutModeChanged(mode: String) {
        logEvent("layout_mode_changed", mapOf("mode" to mode))
    }
}
