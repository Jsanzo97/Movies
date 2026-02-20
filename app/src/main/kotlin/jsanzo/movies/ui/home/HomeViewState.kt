package jsanzo.movies.ui.home

import jsanzo.movies.domain.entity.MovieResult

sealed class HomeViewState

object InitialState: HomeViewState()
object RetrievingMovies: HomeViewState()
class MoviesRetrieved(val movies: List<MovieResult>): HomeViewState()
object SavingMovie: HomeViewState()
class SavedMovie(val movieId: Int): HomeViewState()
class ErrorInOperation(val message: String): HomeViewState()