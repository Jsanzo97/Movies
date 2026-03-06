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
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import jsanzo.movies.R
import jsanzo.movies.presentation.DetailsViewModel
import jsanzo.movies.ui.PreviewOnDevices
import jsanzo.movies.ui.theme.MoviesTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailsScreen(
    movieId: Int,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(movieId) {
        viewModel.getDetails(movieId)
        viewModel.trackScreenView(movieId)
    }

    DetailsScreenContent(
        state = state,
        modifier = modifier,
    )
}

@Composable
private fun DetailsScreenContent(
    state: DetailsViewState,
    modifier: Modifier = Modifier,
) {
    val movieDetailsTitle = stringResource(R.string.movie_details_title)
    Surface(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                paneTitle = if (state is DetailsSuccess) state.movieDetails.title else movieDetailsTitle
            },
        color = MaterialTheme.colorScheme.background,
    ) {
        when (state) {
            is Loading -> {
                val loadingMessage = stringResource(R.string.movie_details_loading)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics { contentDescription = loadingMessage },
                    )
                }
            }

            is DetailsSuccess -> DetailsContent(
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
}

@Suppress("LongMethod")
@Composable
private fun DetailsContent(
    movieDetails: MovieDetailsUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movieDetails.posterPath)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_error_load),
                error = painterResource(R.drawable.ic_error_load),
                modifier = Modifier
                    .width(120.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = movieDetails.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                movieDetails.tagline?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                InfoChip(label = stringResource(R.string.movie_details_punctuation), value = movieDetails.voteAverage.toString())
                InfoChip(label = stringResource(R.string.movie_details_release_date), value = movieDetails.releaseDate)
                InfoChip(label = stringResource(R.string.movie_details_language), value = movieDetails.spokenLanguages)
                movieDetails.runtime?.let {
                    InfoChip(
                        label = stringResource(R.string.movie_details_runtime),
                        value = stringResource(R.string.movie_details_duration_value, it),
                    )
                }
                InfoChip(label = stringResource(R.string.movie_details_genres), value = movieDetails.genres)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            movieDetails.overview?.takeIf { it.isNotBlank() }?.let {
                DetailSection(title = stringResource(R.string.movie_details_overview), body = it)
            }

            DetailSection(
                title = stringResource(R.string.movie_details_production),
                body = movieDetails.productionCompanies,
            )

            DetailSection(
                title = stringResource(R.string.movie_details_country),
                body = movieDetails.productionCountries,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text(
                        text = stringResource(R.string.movie_details_status),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = movieDetails.status,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.movie_details_revenue),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.movie_details_revenue_value, movieDetails.revenue),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            movieDetails.homepage?.takeIf { it.isNotBlank() }?.let { homepage ->
                val uriHandler = LocalUriHandler.current
                val linkDescription = stringResource(R.string.movie_details_webpage_link_description)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.movie_details_webpage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = homepage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .semantics {
                                contentDescription = "$linkDescription: $homepage"
                                role = Role.Button
                            }
                            .clickable { uriHandler.openUri(homepage) },
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun DetailSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
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
            DetailsScreenContent(state = state)
        }
    }
}

private class DetailsViewStateProvider : PreviewParameterProvider<DetailsViewState> {
    override val values: Sequence<DetailsViewState> = sequenceOf(
        Loading,
        DetailsSuccess(
            movieDetails = MovieDetailsUi(
                adult = false,
                backdropPath = null,
                budget = 200_000_000,
                genres = "Fantasy, Adventure",
                homepage = "https://www.harrypotter.com",
                id = 1,
                originalLanguage = "en",
                originalTitle = "Harry Potter and the Deathly Hallows",
                overview = "Harry, Ron and Hermione search for Voldemort's remaining horcruxes in their effort to destroy the Dark Lord.",
                popularity = 150.0,
                posterPath = null,
                productionCompanies = "Warner Bros.",
                productionCountries = "United States of America",
                releaseDate = "2011-07-15",
                revenue = 1_341_511_219,
                runtime = 130,
                spokenLanguages = "English",
                status = "Released",
                tagline = "It all ends here.",
                title = "Harry Potter and the Deathly Hallows: Part 2",
                video = false,
                voteAverage = 8.3,
                voteCount = 18_000,
            ),
        ),
        DetailsError("An unexpected error occurred"),
    )
}
