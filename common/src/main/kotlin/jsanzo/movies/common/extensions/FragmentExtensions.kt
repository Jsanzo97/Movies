package jsanzo.movies.common.extensions

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class LazyFragmentViewBinder<T>(
    private val fragment: Fragment,
    private val idRes: Int
): Lazy<T>, LifecycleObserver {

    private var _value: Any? = null

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment, {
            it.lifecycle.addObserver(this)
        })
    }

    override fun isInitialized() = (null == _value)

    override val value: T get() {
        if (_value == null) {
            _value = fragment.requireView().findViewById(idRes)
        }

        @Suppress("UNCHECKED_CAST")
        return _value as T
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun reset() {
        _value = null
    }
}

fun <T: View> Fragment.lazyBindView(@IdRes idRes: Int): LazyFragmentViewBinder<T> {
    return LazyFragmentViewBinder(this, idRes)
}