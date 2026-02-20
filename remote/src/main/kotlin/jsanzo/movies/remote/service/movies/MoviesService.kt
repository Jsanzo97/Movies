package jsanzo.movies.remote.service.movies

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import jsanzo.movies.data.datastore.RemoteMoviesDatastore
import jsanzo.movies.data.entity.DataMovie
import jsanzo.movies.data.error.RemoteDataError
import jsanzo.movies.remote.dto.response.toDataMovie
import jsanzo.movies.remote.dto.response.toDataMovieDetails
import jsanzo.movies.remote.service.executeNetworkRequest

class MoviesService(
    private val moviesRemoteWebService: MoviesRemoteWebService,
    private val apiKey: String
): RemoteMoviesDatastore {

    override suspend fun getMovies(page: Int): Either<RemoteDataError, DataMovie> =
        executeNetworkRequest {
            moviesRemoteWebService.getMovies(page, apiKey)
        }.fold(
            ifLeft = { error ->
                error.left()
            },
            ifRight = { response ->
                response.toDataMovie().right()
            }
        )

    override suspend fun getMovieDetails(movieId: Int) =
        executeNetworkRequest {
            moviesRemoteWebService.getMovieDetails(movieId, apiKey)
        }.fold(
            ifLeft = { error ->
                error.left()
            },
            ifRight = { response ->
                response.toDataMovieDetails().right()
            }
        )
}