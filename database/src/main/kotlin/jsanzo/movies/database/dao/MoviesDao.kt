package jsanzo.movies.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jsanzo.movies.database.entity.MovieDetailsEntity
import jsanzo.movies.database.entity.MovieEntity

@Dao
interface MoviesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovie(movieEntity: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovieDetails(movieDetailsEntity: MovieDetailsEntity)

    @Query("select * from Movies")
    fun getMovies(): List<MovieEntity>

    @Query("SELECT * FROM Movies WHERE title LIKE '%' || :query || '%'")
    fun searchMovies(query: String): List<MovieEntity>

    @Query("select * from `Movie details` where id == :movieId")
    fun getMovieDetails(movieId: Int): MovieDetailsEntity
}
