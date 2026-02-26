package jsanzo.movies.database

import io.kotest.matchers.shouldBe
import jsanzo.movies.database.entity.MovieCollectionEntity
import jsanzo.movies.database.entity.MovieGenreEntity
import jsanzo.movies.database.entity.MovieProductionCompanyEntity
import jsanzo.movies.database.entity.MovieProductionCountryEntity
import jsanzo.movies.database.entity.MovieSpokenLanguageEntity
import org.junit.jupiter.api.Test

class LocalDataBaseTest {

    private val converters = Converters()

    @Test
    fun `fromIntEntityList converts json to list`() {
        val json = "[1,2,3]"
        val result = converters.fromIntEntityList(json)
        result shouldBe listOf(1, 2, 3)
    }

    @Test
    fun `toIntEntityList converts list to json`() {
        val list = listOf(1, 2, 3)
        val result = converters.toIntEntityList(list)
        result shouldBe "[1,2,3]"
    }

    @Test
    fun `fromMovieCollectionEntity converts json to entity`() {
        val json = """{"id":1,"name":"collection","posterPath":"path","backdropPath":"backdrop"}"""
        val result = converters.fromMovieCollectionEntity(json)
        result shouldBe MovieCollectionEntity(1, "collection", "path", "backdrop")
    }

    @Test
    fun `fromMovieCollectionEntity returns null for null input`() {
        converters.fromMovieCollectionEntity(null) shouldBe null
    }

    @Test
    fun `toMovieCollectionEntity converts entity to json`() {
        val entity = MovieCollectionEntity(1, "collection", "path", "backdrop")
        val result = converters.toMovieCollectionEntity(entity)
        result shouldBe """{"id":1,"name":"collection","posterPath":"path","backdropPath":"backdrop"}"""
    }

    @Test
    fun `toMovieCollectionEntity returns null for null input`() {
        converters.toMovieCollectionEntity(null) shouldBe null
    }

    @Test
    fun `fromMovieGenreEntityList converts json to list`() {
        val json = """[{"id":1,"name":"genre"}]"""
        val result = converters.fromMovieGenreEntityList(json)
        result shouldBe listOf(MovieGenreEntity(1, "genre"))
    }

    @Test
    fun `toMovieGenreEntityList converts list to json`() {
        val list = listOf(MovieGenreEntity(1, "genre"))
        val result = converters.toMovieGenreEntityList(list)
        result shouldBe """[{"id":1,"name":"genre"}]"""
    }

    @Test
    fun `fromMovieProductionCompanyEntityList converts json to list`() {
        val json = """[{"name":"name","id":1,"logoPath":"path","originCountry":"US"}]"""
        val result = converters.fromMovieProductionCompanyEntityList(json)
        result shouldBe listOf(MovieProductionCompanyEntity("name", 1, "path", "US"))
    }

    @Test
    fun `toMovieProductionCompanyEntityList converts list to json`() {
        val list = listOf(MovieProductionCompanyEntity("name", 1, "path", "US"))
        val result = converters.toMovieProductionCompanyEntityList(list)
        result shouldBe """[{"name":"name","id":1,"logoPath":"path","originCountry":"US"}]"""
    }

    @Test
    fun `fromMovieProductionCountryEntityList converts json to list`() {
        val json = """[{"iso":"US","name":"USA"}]"""
        val result = converters.fromMovieProductionCountryEntityList(json)
        result shouldBe listOf(MovieProductionCountryEntity("US", "USA"))
    }

    @Test
    fun `toMovieProductionCountryEntityList converts list to json`() {
        val list = listOf(MovieProductionCountryEntity("US", "USA"))
        val result = converters.toMovieProductionCountryEntityList(list)
        result shouldBe """[{"iso":"US","name":"USA"}]"""
    }

    @Test
    fun `fromMovieSpokenLanguageEntityList converts json to list`() {
        val json = """[{"englishName":"english","iso":"en","name":"English"}]"""
        val result = converters.fromMovieSpokenLanguageEntityList(json)
        result shouldBe listOf(MovieSpokenLanguageEntity("english", "en", "English"))
    }

    @Test
    fun `toMovieSpokenLanguageEntityList converts list to json`() {
        val list = listOf(MovieSpokenLanguageEntity("english", "en", "English"))
        val result = converters.toMovieSpokenLanguageEntityList(list)
        result shouldBe """[{"englishName":"english","iso":"en","name":"English"}]"""
    }
}
