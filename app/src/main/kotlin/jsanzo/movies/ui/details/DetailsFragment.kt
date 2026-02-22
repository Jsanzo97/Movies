package jsanzo.movies.ui.details

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.textview.MaterialTextView
import jsanzo.movies.R
import jsanzo.movies.common.extensions.lazyBindView
import jsanzo.movies.common.extensions.toFormattedString
import jsanzo.movies.common.fragment.CustomFragment
import jsanzo.movies.common.view.MediaView
import jsanzo.movies.domain.model.DomainMovieDetails
import jsanzo.movies.domain.model.DomainMovieGenre
import jsanzo.movies.domain.model.DomainMovieProductionCompany
import jsanzo.movies.domain.model.DomainMovieProductionCountry
import jsanzo.movies.domain.model.DomainMovieSpokenLanguage
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.map

class DetailsFragment : CustomFragment(R.layout.details_fragment) {

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
                        updateDetails(state.domainMovieDetails)
                        hideProgressDialog()
                    }

                    is ErrorInOperation -> showError(state.message)
                }
            }
        }
    }

    private fun updateDetails(domainMovieDetails: DomainMovieDetails) {
        movieDetailsImage.loadImage(domainMovieDetails.posterPath)
        movieDetailsTitle.text = domainMovieDetails.title
        movieDetailsPunctuation.text = domainMovieDetails.voteAverage.toString()
        movieDetailsReleaseDate.text = domainMovieDetails.releaseDate
        movieDetailsLanguage.text = formatLanguages(domainMovieDetails.spokenLanguages)
        movieDetailsDuration.text = getString(R.string.movie_details_duration_value, domainMovieDetails.runtime)
        movieDetailsGenres.text = formatGenres(domainMovieDetails.genres)
        movieDetailsWebpage.text = domainMovieDetails.homepage
        movieDetailsOverview.text = domainMovieDetails.overview
        movieDetailsProduction.text = formatProductions(domainMovieDetails.productionCompanies)
        movieDetailsCountry.text = formatCountries(domainMovieDetails.productionCountries)
        movieDetailsStatus.text = domainMovieDetails.status
        movieDetailsRevenue.text = getString(R.string.movie_details_revenue_value, domainMovieDetails.revenue)
    }

    private fun formatLanguages(languages: List<DomainMovieSpokenLanguage>) = languages.map { language ->
        language.name
    }.toFormattedString()

    private fun formatGenres(genres: List<DomainMovieGenre>) = genres.map { genre ->
        genre.name
    }.toFormattedString()

    private fun formatProductions(productions: List<DomainMovieProductionCompany>) = productions.map { productionCompany ->
        productionCompany.name
    }.toFormattedString()

    private fun formatCountries(countries: List<DomainMovieProductionCountry>) = countries.map { productionCountry ->
        productionCountry.name
    }.toFormattedString()
}
