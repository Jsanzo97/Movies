package jsanzo.movies.domain.usecase

import jsanzo.movies.domain.repository.MoviesRepository

class GetMoviesUseCase(private val movieRepository: MoviesRepository) {

    suspend operator fun invoke(page: Int) = movieRepository.getMovies(page)

}