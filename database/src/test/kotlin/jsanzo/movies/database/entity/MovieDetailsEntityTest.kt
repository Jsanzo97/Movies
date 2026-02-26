package jsanzo.movies.database.entity

import io.kotest.matchers.shouldBe
import jsanzo.movies.database.model.dataMovieDetails
import jsanzo.movies.database.model.movieDetailsEntity
import org.junit.jupiter.api.Test

class MovieDetailsEntityTest {

    @Test
    fun `toMovieDetailsEntity maps correctly`() {
        val result = dataMovieDetails.toMovieDetailsEntity()

        result.adult shouldBe dataMovieDetails.adult
        result.backdropPath shouldBe dataMovieDetails.backdropPath
        result.belongsToCollection?.id shouldBe dataMovieDetails.belongsToCollection?.id
        result.budget shouldBe dataMovieDetails.budget
        result.genres.size shouldBe dataMovieDetails.genres.size
        result.homepage shouldBe dataMovieDetails.homepage
        result.id shouldBe dataMovieDetails.id
        result.imdbId shouldBe dataMovieDetails.imdbId
        result.originCountry shouldBe "US,ES"
        result.originalLanguage shouldBe dataMovieDetails.originalLanguage
        result.originalTitle shouldBe dataMovieDetails.originalTitle
        result.overview shouldBe dataMovieDetails.overview
        result.popularity shouldBe dataMovieDetails.popularity
        result.posterPath shouldBe dataMovieDetails.posterPath
        result.productionCompanies.size shouldBe dataMovieDetails.productionCompanies.size
        result.productionCountries.size shouldBe dataMovieDetails.productionCountries.size
        result.releaseDate shouldBe dataMovieDetails.releaseDate
        result.revenue shouldBe dataMovieDetails.revenue
        result.runtime shouldBe dataMovieDetails.runtime
        result.spokenLanguages.size shouldBe dataMovieDetails.spokenLanguages.size
        result.status shouldBe dataMovieDetails.status
        result.tagline shouldBe dataMovieDetails.tagline
        result.title shouldBe dataMovieDetails.title
        result.video shouldBe dataMovieDetails.video
        result.voteAverage shouldBe dataMovieDetails.voteAverage
        result.voteCount shouldBe dataMovieDetails.voteCount
    }

    @Test
    fun `toMovieDetailsEntity maps correctly with nulls`() {
        val dataWithNulls = dataMovieDetails.copy(belongsToCollection = null, backdropPath = null)
        val result = dataWithNulls.toMovieDetailsEntity()

        result.belongsToCollection shouldBe null
        result.backdropPath shouldBe null
    }

    @Test
    fun `toDataMovieDetails maps correctly`() {
        val result = movieDetailsEntity.toDataMovieDetails()

        result.adult shouldBe movieDetailsEntity.adult
        result.backdropPath shouldBe movieDetailsEntity.backdropPath
        result.belongsToCollection?.id shouldBe movieDetailsEntity.belongsToCollection?.id
        result.budget shouldBe movieDetailsEntity.budget
        result.genres.size shouldBe movieDetailsEntity.genres.size
        result.homepage shouldBe movieDetailsEntity.homepage
        result.id shouldBe movieDetailsEntity.id
        result.imdbId shouldBe movieDetailsEntity.imdbId
        result.originCountry shouldBe listOf("US", "ES")
        result.originalLanguage shouldBe movieDetailsEntity.originalLanguage
        result.originalTitle shouldBe movieDetailsEntity.originalTitle
        result.overview shouldBe movieDetailsEntity.overview
        result.popularity shouldBe movieDetailsEntity.popularity
        result.posterPath shouldBe movieDetailsEntity.posterPath
        result.productionCompanies.size shouldBe movieDetailsEntity.productionCompanies.size
        result.productionCountries.size shouldBe movieDetailsEntity.productionCountries.size
        result.releaseDate shouldBe movieDetailsEntity.releaseDate
        result.revenue shouldBe movieDetailsEntity.revenue
        result.runtime shouldBe movieDetailsEntity.runtime
        result.spokenLanguages.size shouldBe movieDetailsEntity.spokenLanguages.size
        result.status shouldBe movieDetailsEntity.status
        result.tagline shouldBe movieDetailsEntity.tagline
        result.title shouldBe movieDetailsEntity.title
        result.video shouldBe movieDetailsEntity.video
        result.voteAverage shouldBe movieDetailsEntity.voteAverage
        result.voteCount shouldBe movieDetailsEntity.voteCount
    }

    @Test
    fun `toDataMovieDetails maps correctly with nulls`() {
        val entityWithNulls = movieDetailsEntity.copy(belongsToCollection = null, backdropPath = null)
        val result = entityWithNulls.toDataMovieDetails()

        result.belongsToCollection shouldBe null
        result.backdropPath shouldBe null
    }
}
