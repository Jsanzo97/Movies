package jsanzo.movies.remote.service.movies

import arrow.core.Either
import jsanzo.movies.data.datastore.RemoteMoviesDatastore
import jsanzo.movies.data.error.DataError
import jsanzo.movies.data.model.DataMovie
import jsanzo.movies.data.model.DataMovieDetails
import jsanzo.movies.remote.dto.response.toDataMovie
import jsanzo.movies.remote.dto.response.toDataMovieDetails
import jsanzo.movies.remote.service.NetworkHandler
import org.koin.core.annotation.Single

@Single
class MoviesService(
    private val moviesRemoteWebService: MoviesRemoteWebService,
    private val networkHandler: NetworkHandler,
    private val apiKey: String,
) : RemoteMoviesDatastore {

    override suspend fun getMovies(page: Int): Either<DataError, DataMovie> = networkHandler.executeNetworkRequest {
        moviesRemoteWebService.getMovies(page, apiKey)
    }.map { response ->
        response.toDataMovie()
    }

    override suspend fun getMovieDetails(movieId: Int): Either<DataError, DataMovieDetails> = networkHandler.executeNetworkRequest {
        moviesRemoteWebService.getMovieDetails(movieId, apiKey)
    }.map { response ->
        response.toDataMovieDetails()
    }

    override suspend fun searchMovies(query: String): Either<DataError, DataMovie> = networkHandler.executeNetworkRequest {
        moviesRemoteWebService.searchMovies(query, apiKey)
    }.map { response ->
        response.toDataMovie()
    }
}
