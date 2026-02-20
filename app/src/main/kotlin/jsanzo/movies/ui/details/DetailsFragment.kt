package jsanzo.movies.ui.details

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import jsanzo.movies.common.extensions.lazyBindView
import jsanzo.movies.common.extensions.toFormattedString
import jsanzo.movies.common.fragment.CustomFragment
import jsanzo.movies.common.view.MediaView
import jsanzo.movies.domain.entity.*
import jsanzo.movies.R
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailsFragment: CustomFragment(R.layout.details_fragment) {

    private val viewModel: DetailsViewModel by viewModel()
    private val args: DetailsFragmentArgs by navArgs()

    private val movieDetailsImage: MediaView by lazyBindView(R.id.movie_details_image)
    private val movieDetailsTitle: MaterialTextView by lazyBindView(R.id.movie_details_title_value_text)
    private val movieDetailsPunctuation: MaterialTextView by lazyBindView(R.id.movie_details_punctuation_value_text)
    private val movieDetailsReleaseDate: MaterialTextView by lazyBindView(R.id.movie_details_release_date_value_text)
    private val movieDetailsLanguage: MaterialTextView by lazyBindView(R.id.movie_details_language_value_text)
    private val movieDetailsDuration: MaterialTextView by lazyBindView(R.id.movie_details_runtime_value_text)
    private val movieDetailsGenres: MaterialTextView by lazyBindView(R.id.movie_details_genres_value_text)
    private val movieDetailsWebpage: MaterialTextView by lazyBindView(R.id.movie_details_webpage_value_text)
    private val movieDetailsOverview: MaterialTextView by lazyBindView(R.id.movie_details_overview_value_text)
    private val movieDetailsProduction: MaterialTextView by lazyBindView(R.id.movie_details_production_value_text)
    private val movieDetailsCountry: MaterialTextView by lazyBindView(R.id.movie_details_country_value_text)
    private val movieDetailsStatus: MaterialTextView by lazyBindView(R.id.movie_details_status_value_text)
    private val movieDetailsRevenue: MaterialTextView by lazyBindView(R.id.movie_details_revenue_value_text)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModelStateFlow()

        viewModel.getDetails(args.movieId)
    }

    private fun setupViewModelStateFlow() {
        lifecycleScope.launchWhenStarted {
            viewModel.detailsViewModelSateFlow.collect { state ->
                when (state) {
                    is InitialState -> { /* no-op */ }
                    is RetrievingDetails -> showProgressDialog()
                    is DetailsRetrieved -> {
                        updateDetails(state.movieDetails)
                        hideProgressDialog()
                    }
                    is ErrorInOperation -> showError(state.message)
                }
            }
        }
    }

    private fun updateDetails(movieDetails: MovieDetails) {
        movieDetailsImage.loadImage(movieDetails.posterPath)
        movieDetailsTitle.text = movieDetails.title
        movieDetailsPunctuation.text = movieDetails.voteAverage.toString()
        movieDetailsReleaseDate.text = movieDetails.releaseDate
        movieDetailsLanguage.text = formatLanguages(movieDetails.spokenLanguages)
        movieDetailsDuration.text = getString(R.string.movie_details_duration_value, movieDetails.runtime)
        movieDetailsGenres.text = formatGenres(movieDetails.genres)
        movieDetailsWebpage.text = movieDetails.homepage
        movieDetailsOverview.text = movieDetails.overview
        movieDetailsProduction.text = formatProductions(movieDetails.productionCompanies)
        movieDetailsCountry.text = formatCountries(movieDetails.productionCountries)
        movieDetailsStatus.text = movieDetails.status
        movieDetailsRevenue.text = getString(R.string.movie_details_revenue_value, movieDetails.revenue)
    }

    private fun formatLanguages(languages: List<MovieSpokenLanguage>) =
        languages.map { language ->
            language.name
        }.toFormattedString()


    private fun formatGenres(genres: List<MovieGenre>) =
        genres.map { genre ->
            genre.name
        }.toFormattedString()


    private fun formatProductions(productions: List<MovieProductionCompany>) =
        productions.map { productionCompany ->
            productionCompany.name
        }.toFormattedString()

    private fun formatCountries(countries: List<MovieProductionCountry>) =
        countries.map { productionCountry ->
            productionCountry.name
        }.toFormattedString()

}