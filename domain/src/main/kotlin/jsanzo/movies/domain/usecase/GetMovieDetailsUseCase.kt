package jsanzo.movies.domain.usecase

import jsanzo.movies.domain.repository.MoviesRepository

class GetMovieDetailsUseCase(private val movieRepository: MoviesRepository) {

    suspend operator fun invoke(movieId: Int) = movieRepository.getMovieDetails(movieId)

}