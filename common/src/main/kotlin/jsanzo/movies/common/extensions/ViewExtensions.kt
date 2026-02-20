package jsanzo.movies.common.extensions

import android.view.View
import androidx.core.view.isVisible

fun View.changeVisibility(visible: Boolean) {
    if (this.isVisible == visible) {
        return
    }

    this.visibility = if (visible) View.VISIBLE else View.GONE
}