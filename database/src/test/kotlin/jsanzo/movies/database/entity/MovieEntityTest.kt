package jsanzo.movies.database.entity

import io.kotest.matchers.shouldBe
import jsanzo.movies.database.model.dataMovieResult
import jsanzo.movies.database.model.movieEntity
import org.junit.jupiter.api.Test

class MovieEntityTest {

    @Test
    fun `toMovieEntity maps correctly`() {
        val result = dataMovieResult.toMovieEntity()

        result.posterPath shouldBe dataMovieResult.posterPath
        result.adult shouldBe dataMovieResult.adult
        result.overview shouldBe dataMovieResult.overview
        result.releaseDate shouldBe dataMovieResult.releaseDate
        result.genreIds shouldBe dataMovieResult.genreIds
        result.id shouldBe dataMovieResult.id
        result.originalTitle shouldBe dataMovieResult.originalTitle
        result.originalLanguage shouldBe dataMovieResult.originalLanguage
        result.title shouldBe dataMovieResult.title
        result.backdropPath shouldBe dataMovieResult.backdropPath
        result.popularity shouldBe dataMovieResult.popularity
        result.voteCount shouldBe dataMovieResult.voteCount
        result.video shouldBe dataMovieResult.video
        result.voteAverage shouldBe dataMovieResult.voteAverage
    }

    @Test
    fun `toDataMovieResult maps correctly`() {
        val result = movieEntity.toDataMovieResult()

        result.posterPath shouldBe movieEntity.posterPath
        result.adult shouldBe movieEntity.adult
        result.overview shouldBe movieEntity.overview
        result.releaseDate shouldBe movieEntity.releaseDate
        result.genreIds shouldBe movieEntity.genreIds
        result.id shouldBe movieEntity.id
        result.originalTitle shouldBe movieEntity.originalTitle
        result.originalLanguage shouldBe movieEntity.originalLanguage
        result.title shouldBe movieEntity.title
        result.backdropPath shouldBe movieEntity.backdropPath
        result.popularity shouldBe movieEntity.popularity
        result.voteCount shouldBe movieEntity.voteCount
        result.video shouldBe movieEntity.video
        result.voteAverage shouldBe movieEntity.voteAverage
    }
}
