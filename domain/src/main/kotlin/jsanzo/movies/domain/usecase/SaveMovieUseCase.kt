package jsanzo.movies.domain.usecase

import jsanzo.movies.domain.model.DomainMovie
import jsanzo.movies.domain.repository.MoviesRepository

class SaveMovieUseCase(private val movieRepository: MoviesRepository) {

    suspend operator fun invoke(movie: DomainMovie) = movieRepository.saveMovie(movie)
}
