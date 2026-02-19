package com.example.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.database.dao.MoviesDao
import com.example.database.entity.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Database(
    entities = [
        MovieEntity::class,
        MovieDetailsEntity::class
    ],
    version = 1
)
@TypeConverters(
    Converters::class
)
abstract class LocalDatabase: RoomDatabase() {
    abstract fun moviesDao(): MoviesDao
}

class Converters {
    @TypeConverter
    fun fromIntEntityList(value: String): List<Int> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun toIntEntityList(value: List<Int>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun fromMovieCollectionEntity(value: String?): MovieCollectionEntity? {
        return value?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun toMovieCollectionEntity(value: MovieCollectionEntity?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun fromMovieGenreEntityList(value: String): List<MovieGenreEntity> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun toMovieGenreEntityList(value: List<MovieGenreEntity>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun fromMovieProductionCompanyEntityList(value: String): List<MovieProductionCompanyEntity> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun toMovieProductionCompanyEntityList(value: List<MovieProductionCompanyEntity>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun fromMovieProductionCountryEntityList(value: String): List<MovieProductionCountryEntity> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun toMovieProductionCountryEntityList(value: List<MovieProductionCountryEntity>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toMovieSpokenLanguageEntityList(value: List<MovieSpokenLanguageEntity>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun fromMovieSpokenLanguageEntityList(value: String): List<MovieSpokenLanguageEntity> {
        return Json.decodeFromString(value)
    }
}