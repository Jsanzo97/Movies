package jsanzo.movies.ui.home

import jsanzo.movies.domain.model.DomainMovieResult

sealed class HomeViewState

object InitialState : HomeViewState()
object RetrievingMovies : HomeViewState()
class MoviesRetrieved(val movies: List<DomainMovieResult>) : HomeViewState()
object SavingMovie : HomeViewState()
class SavedMovie(val movieId: Int) : HomeViewState()
class ErrorInOperation(val message: String) : HomeViewState()
