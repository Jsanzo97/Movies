package jsanzo.movies.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import jsanzo.movies.R
import jsanzo.movies.common.view.MediaView
import jsanzo.movies.domain.entity.MovieResult

class HomeMovieAdapter(
    private val listener: HomeMoviesAdapterListener,
) : ListAdapter<MovieResult, HomeMovieAdapter.ViewHolder>(HomeMoviesDiffUtilCallback()), Filterable {

    private val movieList = mutableListOf<MovieResult>()
    private var filteredList = listOf<MovieResult>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.home_movie_view,
            viewGroup,
            false,
        )
        return ViewHolder(v)
    }

    fun onNewData(movies: List<MovieResult>) {
        movies.forEach { movieResult ->
            if (!movieList.contains(movieResult)) {
                movieList.add(movieResult)
            }
        }

        filteredList = movieList
        submitList(filteredList)
    }

    override fun onBindViewHolder(item: ViewHolder, position: Int) {
        val element = getItem(position)
        item.movieTitleLabel.text = element.title
        item.movieImage.loadImage(element.posterPath)
        item.movieReleaseDate.text = element.releaseDate
        item.movieVoteAverage.text = element.voteAverage.toString()
        item.movieLanguage.text = element.originalLanguage.uppercase()
        item.movieLayout.setOnClickListener { listener.onItemClick(element) }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(titleRestriction: CharSequence): FilterResults {
                filteredList = movieList.filter { movieResult ->
                    movieResult.title.contains(titleRestriction, ignoreCase = true)
                }

                return FilterResults().apply {
                    values = filteredList
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                filteredList = if (results.values == null) {
                    mutableListOf()
                } else {
                    @Suppress("UNCHECKED_CAST")
                    results.values as List<MovieResult>
                }
                submitList(filteredList)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val movieTitleLabel: MaterialTextView = itemView.findViewById(R.id.movie_title_value_text)
        val movieImage: MediaView = itemView.findViewById(R.id.movie_image)
        val movieReleaseDate: MaterialTextView = itemView.findViewById(R.id.movie_release_date_value_text)
        val movieVoteAverage: MaterialTextView = itemView.findViewById(R.id.movie_vote_average_value_text)
        val movieLanguage: MaterialTextView = itemView.findViewById(R.id.movie_language_value_text)
        val movieLayout: MaterialCardView = itemView.findViewById(R.id.movie_layout)
    }
}
