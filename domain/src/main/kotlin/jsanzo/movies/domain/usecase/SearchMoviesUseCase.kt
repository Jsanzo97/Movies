package jsanzo.movies.domain.usecase

import jsanzo.movies.domain.repository.MoviesRepository

class SearchMoviesUseCase(private val repository: MoviesRepository) {
    suspend operator fun invoke(query: String) = repository.searchMovies(query)
}
