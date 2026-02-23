package jsanzo.movies.ui.screens.details

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import jsanzo.movies.R
import jsanzo.movies.domain.model.DomainMovieDetails
import jsanzo.movies.domain.model.DomainMovieGenre
import jsanzo.movies.domain.model.DomainMovieProductionCompany
import jsanzo.movies.domain.model.DomainMovieProductionCountry
import jsanzo.movies.domain.model.DomainMovieSpokenLanguage
import jsanzo.movies.ui.theme.MoviesTheme
import org.koin.androidx.compose.koinViewModel

private const val BASE_IMAGE_URL_ORIGINAL = "https://image.tmdb.org/t/p/original"

@Composable
fun DetailsScreen(
    movieId: Int,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(movieId) {
        viewModel.getDetails(movieId)
    }

    when (val currentState = state) {
        is Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is DetailsSuccess -> DetailsContent(
            movieDetails = currentState.movieDetails,
            modifier = modifier,
        )

        is DetailsError -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = currentState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun DetailsContent(
    movieDetails: DomainMovieDetails,
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
            SubcomposeAsyncImage(
                model = movieDetails.posterPath?.let { BASE_IMAGE_URL_ORIGINAL + it },
                contentDescription = movieDetails.title,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_error_load),
                            contentDescription = null,
                        )
                    }
                },
                modifier = Modifier
                    .width(130.dp)
                    .height(195.dp)
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
                InfoChip(label = stringResource(R.string.movie_details_language), value = formatLanguages(movieDetails.spokenLanguages))
                movieDetails.runtime?.let {
                    InfoChip(
                        label = stringResource(R.string.movie_details_runtime),
                        value = stringResource(R.string.movie_details_duration_value, it),
                    )
                }
                InfoChip(label = stringResource(R.string.movie_details_genres), value = formatGenres(movieDetails.genres))
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
                body = formatProductions(movieDetails.productionCompanies),
            )

            DetailSection(
                title = stringResource(R.string.movie_details_country),
                body = formatCountries(movieDetails.productionCountries),
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
                        modifier = Modifier.clickable { uriHandler.openUri(homepage) },
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun formatLanguages(languages: List<DomainMovieSpokenLanguage>) = languages.joinToString(", ") { it.name }

private fun formatGenres(genres: List<DomainMovieGenre>) = genres.joinToString(", ") { it.name }

private fun formatProductions(productions: List<DomainMovieProductionCompany>) = productions.joinToString(", ") { it.name }

private fun formatCountries(countries: List<DomainMovieProductionCountry>) = countries.joinToString(", ") { it.name }

@Preview(showBackground = true)
@Composable
private fun DetailsContentPreview() {
    MoviesTheme {
        Surface {
            DetailsContent(
                movieDetails = DomainMovieDetails(
                    adult = false,
                    backdropPath = null,
                    belongsToCollection = null,
                    budget = 200_000_000,
                    genres = listOf(
                        DomainMovieGenre(1, "Fantasy"),
                        DomainMovieGenre(2, "Adventure"),
                    ),
                    homepage = "https://www.harrypotter.com",
                    id = 1,
                    imdbId = "tt1201607",
                    originalLanguage = "en",
                    originalTitle = "Harry Potter and the Deathly Hallows",
                    overview = "Harry, Ron and Hermione search for Voldemort's remaining Horcruxes in their effort to destroy the Dark Lord.",
                    popularity = 8.5,
                    posterPath = null,
                    productionCompanies = listOf(
                        DomainMovieProductionCompany(
                            name = "Warner Bros.",
                            id = 1,
                            logoPath = null,
                            originCountry = "US",
                        ),
                    ),
                    productionCountries = listOf(
                        DomainMovieProductionCountry("GB", "United Kingdom"),
                    ),
                    releaseDate = "2011-07-15",
                    revenue = 1_341_693_157,
                    runtime = 130,
                    spokenLanguages = listOf(
                        DomainMovieSpokenLanguage("en", "English"),
                    ),
                    status = "Released",
                    tagline = "It all ends.",
                    title = "Harry Potter and the Deathly Hallows – Part 2",
                    video = false,
                    voteAverage = 8.1,
                    voteCount = 19_823,
                ),
            )
        }
    }
}
