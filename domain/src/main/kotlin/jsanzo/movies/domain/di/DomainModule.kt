package jsanzo.movies.domain.di

import jsanzo.movies.domain.repository.MoviesRepository
import jsanzo.movies.domain.repository.RemoteConfigRepository
import jsanzo.movies.domain.usecase.GetMovieDetailsUseCase
import jsanzo.movies.domain.usecase.GetMoviesUseCase
import jsanzo.movies.domain.usecase.MustUpdateUseCase
import jsanzo.movies.domain.usecase.SaveMovieUseCase
import jsanzo.movies.domain.usecase.SearchMoviesUseCase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DomainModule {

    @Single
    fun getMovieDetailsUseCase(moviesRepository: MoviesRepository): GetMovieDetailsUseCase = GetMovieDetailsUseCase(moviesRepository)

    @Single
    fun getMoviesUseCase(moviesRepository: MoviesRepository): GetMoviesUseCase = GetMoviesUseCase(moviesRepository)

    @Single
    fun saveMovieUseCase(moviesRepository: MoviesRepository): SaveMovieUseCase = SaveMovieUseCase(moviesRepository)

    @Single
    fun mustUpdateUseCase(remoteConfigRepository: RemoteConfigRepository): MustUpdateUseCase = MustUpdateUseCase(remoteConfigRepository)

    @Single
    fun searchMoviesUseCase(moviesRepository: MoviesRepository): SearchMoviesUseCase = SearchMoviesUseCase(moviesRepository)
}
