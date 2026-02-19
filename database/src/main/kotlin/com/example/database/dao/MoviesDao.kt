package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.entity.MovieDetailsEntity
import com.example.database.entity.MovieEntity

@Dao
interface MoviesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovie(movieEntity: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovieDetails(movieDetailsEntity: MovieDetailsEntity)

    @Query("select * from Movies")
    fun getMovies(): List<MovieEntity>

    @Query("select * from `Movie details` where id == :movieId")
    fun getMovieDetails(movieId: Int): MovieDetailsEntity

}