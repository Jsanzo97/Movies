package jsanzo.movies.domain.usecase

import jsanzo.movies.domain.entity.MovieResult
import jsanzo.movies.domain.repository.MoviesRepository

class SaveMovieUseCase(private val movieRepository: MoviesRepository) {

    suspend operator fun invoke(movie: MovieResult) = movieRepository.saveMovie(movie)
}
