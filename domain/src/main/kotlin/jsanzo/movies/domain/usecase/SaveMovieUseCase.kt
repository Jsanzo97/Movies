package jsanzo.movies.domain.usecase

import jsanzo.movies.domain.model.DomainMovieResult
import jsanzo.movies.domain.repository.MoviesRepository

class SaveMovieUseCase(private val movieRepository: MoviesRepository) {

    suspend operator fun invoke(movie: DomainMovieResult) = movieRepository.saveMovie(movie)
}
