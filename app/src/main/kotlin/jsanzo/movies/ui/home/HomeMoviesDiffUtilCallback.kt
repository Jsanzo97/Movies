package jsanzo.movies.ui.home

import androidx.recyclerview.widget.DiffUtil
import jsanzo.movies.domain.model.DomainMovieResult

class HomeMoviesDiffUtilCallback : DiffUtil.ItemCallback<DomainMovieResult>() {
    override fun areItemsTheSame(oldItem: DomainMovieResult, newItem: DomainMovieResult): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DomainMovieResult, newItem: DomainMovieResult): Boolean {
        return oldItem == newItem
    }
}
