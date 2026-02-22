package jsanzo.movies.remote.service.movies

import arrow.core.Either
import jsanzo.movies.data.datastore.RemoteMoviesDatastore
import jsanzo.movies.data.error.DataError
import jsanzo.movies.data.model.DataMovie
import jsanzo.movies.data.model.DataMovieDetails
import jsanzo.movies.remote.dto.response.toDataMovie
import jsanzo.movies.remote.dto.response.toDataMovieDetails
import jsanzo.movies.remote.service.executeNetworkRequest

class MoviesService(
    private val moviesRemoteWebService: MoviesRemoteWebService,
    private val apiKey: String,
) : RemoteMoviesDatastore {

    override suspend fun getMovies(page: Int): Either<DataError, DataMovie> = executeNetworkRequest {
        moviesRemoteWebService.getMovies(page, apiKey)
    }.map { response ->
        response.toDataMovie()
    }

    override suspend fun getMovieDetails(movieId: Int): Either<DataError, DataMovieDetails> = executeNetworkRequest {
        moviesRemoteWebService.getMovieDetails(movieId, apiKey)
    }.map { response ->
        response.toDataMovieDetails()
    }
}
