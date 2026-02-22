package jsanzo.movies.ui.home

import jsanzo.movies.domain.model.DomainMovieResult

interface HomeMoviesAdapterListener {

    fun onItemClick(element: DomainMovieResult)
}
