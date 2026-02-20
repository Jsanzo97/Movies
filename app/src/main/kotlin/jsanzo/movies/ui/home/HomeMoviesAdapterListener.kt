package jsanzo.movies.ui.home

import jsanzo.movies.domain.entity.MovieResult

interface HomeMoviesAdapterListener {

    fun onItemClick(element: MovieResult)

}