package jsanzo.movies.ui.screens.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydoves.compose.stability.runtime.IgnoreStabilityReport
import jsanzo.movies.R
import jsanzo.movies.presentation.DetailsViewModel
import jsanzo.movies.ui.PreviewOnDevices
import jsanzo.movies.ui.screens.common.HorizontalInfo
import jsanzo.movies.ui.screens.common.LoadingIndicator
import jsanzo.movies.ui.screens.common.MovieImage
import jsanzo.movies.ui.screens.common.VerticalInfo
import jsanzo.movies.ui.theme.MoviesTheme
import org.koin.androidx.compose.koinViewModel

@IgnoreStabilityReport
@Composable
fun DetailsScreen(
    movieId: Int,
    windowInsets: WindowInsets,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(movieId) {
        viewModel.getDetails(movieId)
        viewModel.trackScreenView(movieId)
    }

    DetailsLayout(
        state = state,
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(windowInsets),
    )
}

@Composable
private fun DetailsLayout(
    state: DetailsViewState,
    modifier: Modifier = Modifier,
) {
    val movieDetailsTitle = stringResource(R.string.movie_details_title)

    Surface(
        modifier = modifier
            .semantics {
                paneTitle = if (state is DetailsSuccess) state.movieDetails.title else movieDetailsTitle
            },
        color = MaterialTheme.colorScheme.background,
    ) {
        DetailsContent(
            state = state,
        )
    }
}

@Composable
private fun DetailsContent(
    state: DetailsViewState,
) {
    when (state) {
        is Loading ->
            LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize(),
                contentDescription = stringResource(R.string.movie_details_loading),
            )

        is DetailsSuccess ->
            DetailsInformation(
                modifier = Modifier.fillMaxSize(),
                movieDetails = state.movieDetails,
            )

        is DetailsError -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun DetailsInformation(
    movieDetails: MovieDetailsUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        with(movieDetails) {
            DetailsHeader(
                modifier = Modifier
                    .fillMaxWidth(),
                posterPath = posterPath,
                title = title,
                tagLine = tagline,
                voteAverage = voteAverage,
                releaseDate = releaseDate,
                spokenLanguages = spokenLanguages,
                runtime = runtime,
                genres = genres,
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 8.dp),
            )

            DetailsBody(
                modifier = Modifier
                    .fillMaxWidth(),
                overview = overview,
                productionCompanies = productionCompanies,
                productionCountries = productionCountries,
                status = status,
                revenue = revenue,
                homePage = homepage,
            )
        }
    }
}

@Composable
private fun DetailsHeader(
    posterPath: String,
    title: String,
    tagLine: String,
    voteAverage: String,
    releaseDate: String,
    spokenLanguages: String,
    runtime: Int,
    genres: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
    ) {
        MovieImage(
            modifier = Modifier
                .width(120.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp)),
            posterPath = posterPath,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )

            if (tagLine.isNotBlank()) {
                Text(
                    text = tagLine,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            HorizontalInfo(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.movie_details_punctuation),
                value = voteAverage,
            )
            HorizontalInfo(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.movie_details_release_date),
                value = releaseDate,
            )
            HorizontalInfo(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.movie_details_language),
                value = spokenLanguages,
            )
            HorizontalInfo(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.movie_details_runtime),
                value = stringResource(R.string.movie_details_duration_value, runtime),
            )
            HorizontalInfo(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.movie_details_genres),
                value = genres,
            )
        }
    }
}

@Composable
private fun DetailsBody(
    overview: String,
    productionCompanies: String,
    productionCountries: String,
    status: String,
    revenue: String,
    homePage: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        VerticalInfo(
            modifier = Modifier
                .fillMaxWidth(),
            label = stringResource(R.string.movie_details_overview),
            value = overview,
        )

        if (productionCompanies.isNotBlank()) {
            VerticalInfo(
                modifier = Modifier
                    .fillMaxWidth(),
                label = stringResource(R.string.movie_details_production),
                value = productionCompanies,
            )
        }

        VerticalInfo(
            modifier = Modifier
                .fillMaxWidth(),
            label = stringResource(R.string.movie_details_country),
            value = productionCountries,
        )

        VerticalInfo(
            modifier = Modifier
                .fillMaxWidth(),
            label = stringResource(R.string.movie_details_status),
            value = status,
        )

        VerticalInfo(
            modifier = Modifier
                .fillMaxWidth(),
            label = stringResource(R.string.movie_details_revenue),
            value = revenue,
        )

        VerticalInfo(
            modifier = Modifier
                .fillMaxWidth(),
            label = stringResource(R.string.movie_details_country),
            value = productionCountries,
        )

        if (homePage.isNotBlank()) {
            WebInfo(
                modifier = Modifier
                    .fillMaxWidth(),
                homePage = homePage,
            )
        }
    }
}

@Composable
private fun WebInfo(
    homePage: String,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val linkDescription = stringResource(R.string.movie_details_webpage_link_description)

    Column(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.movie_details_webpage),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = homePage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .semantics {
                    contentDescription = "$linkDescription: $homePage"
                    role = Role.Button
                }
                .clickable { uriHandler.openUri(homePage) },
        )
    }
}

@Composable
@PreviewOnDevices
private fun DetailsScreenPreview(
    @PreviewParameter(DetailsViewStateProvider::class) state: DetailsViewState,
) {
    MoviesTheme {
        Surface {
            DetailsLayout(state = state)
        }
    }
}

private class DetailsViewStateProvider : PreviewParameterProvider<DetailsViewState> {
    override val values: Sequence<DetailsViewState> = sequenceOf(
        Loading,
        DetailsSuccess(
            movieDetails = MovieDetailsUi(
                adult = false,
                backdropPath = "",
                budget = 200_000_000,
                genres = "Fantasy, Adventure",
                homepage = "https://www.harrypotter.com",
                id = 1,
                originalLanguage = "en",
                originalTitle = "Harry Potter and the Deathly Hallows",
                overview = "Harry, Ron and Hermione search for Voldemort's remaining horcruxes in their effort to destroy the Dark Lord.",
                popularity = 150.0,
                posterPath = "",
                productionCompanies = "Warner Bros.",
                productionCountries = "United States of America",
                releaseDate = "2011-07-15",
                revenue = "$ 1,341,511,219",
                runtime = 130,
                spokenLanguages = "English",
                status = "Released",
                tagline = "It all ends here.",
                title = "Harry Potter and the Deathly Hallows: Part 2",
                video = false,
                voteAverage = "8.3",
                voteCount = 18_000,
            ),
        ),
        DetailsError("An unexpected error occurred"),
    )
}
