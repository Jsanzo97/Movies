package jsanzo.movies.ui.home

import androidx.recyclerview.widget.DiffUtil
import jsanzo.movies.domain.entity.MovieResult

class HomeMoviesDiffUtilCallback : DiffUtil.ItemCallback<MovieResult>() {
    override fun areItemsTheSame(oldItem: MovieResult, newItem: MovieResult): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MovieResult, newItem: MovieResult): Boolean {
        return oldItem == newItem
    }
}
