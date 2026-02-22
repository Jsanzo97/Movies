package jsanzo.movies.database.di

import android.content.Context
import androidx.room.Room
import jsanzo.movies.data.datastore.LocalMoviesDatastore
import jsanzo.movies.database.LocalDatabase
import jsanzo.movies.database.dao.MoviesDao
import jsanzo.movies.database.storage.MoviesStorage
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

private const val DATABASE_NAME = "localStorage.db"

@Module
class DatabaseModule {

    @Single
    fun localMoviesDatastore(moviesDao: MoviesDao): LocalMoviesDatastore = MoviesStorage(moviesDao)

    @Single
    fun localDatabase(context: Context): LocalDatabase = Room.databaseBuilder(context, LocalDatabase::class.java, DATABASE_NAME)
        .fallbackToDestructiveMigration(false)
        .build()

    @Single
    fun moviesDao(database: LocalDatabase): MoviesDao = database.moviesDao()
}
