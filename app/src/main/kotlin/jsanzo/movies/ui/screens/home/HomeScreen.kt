package jsanzo.movies.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import jsanzo.movies.R
import jsanzo.movies.domain.model.DomainMovieResult
import jsanzo.movies.ui.BASE_IMAGE_URL_ORIGINAL
import jsanzo.movies.ui.PreviewOnDevices
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getMovies()
        viewModel.trackScreenView()
    }

    HomeContent(
        state = state,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onMovieClick = {
            viewModel.saveMovie(it)
            onNavigateToDetails(it.id)
        },
        onLastVisibleIndex = { viewModel.notifyLastElementVisible(it) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
private fun HomeContent(
    state: HomeViewState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMovieClick: (DomainMovieResult) -> Unit,
    onLastVisibleIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val movies = remember(state, searchQuery) {
        if (state is MoviesSuccess) {
            state.movies.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
        } else {
            emptyList()
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { onLastVisibleIndex(it) }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text("Search") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    )
                },
                expanded = false,
                onExpandedChange = {},
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {}

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(
                        items = movies,
                        key = { _, movie -> movie.id },
                    ) { _, movie ->
                        MovieItem(
                            movie = movie,
                            onClick = { onMovieClick(movie) },
                        )
                    }
                }

                if (state is Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                if (state is MoviesError) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun MovieItem(
    movie: DomainMovieResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(min = 120.dp),
        ) {
            SubcomposeAsyncImage(
                model = movie.posterPath?.let { BASE_IMAGE_URL_ORIGINAL + it },
                contentDescription = movie.title,
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
                    .width(100.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                LabeledText(
                    label = stringResource(R.string.movie_score),
                    value = movie.voteAverage.toString(),
                )
                LabeledText(
                    label = stringResource(R.string.movie_date),
                    value = movie.releaseDate,
                )
                LabeledText(
                    label = stringResource(R.string.movie_language),
                    value = movie.originalLanguage.uppercase(),
                )
            }
        }
    }
}

@Composable
private fun LabeledText(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
@PreviewOnDevices
private fun HomeContentPreview(
    @PreviewParameter(HomeViewStateProvider::class) state: HomeViewState,
) {
    HomeContent(
        state = state,
        searchQuery = "",
        onSearchQueryChange = {},
        onMovieClick = {},
        onLastVisibleIndex = {},
    )
}

private class HomeViewStateProvider : PreviewParameterProvider<HomeViewState> {
    override val values: Sequence<HomeViewState> = sequenceOf(
        Loading,
        MoviesSuccess(
            movies = listOf(
                DomainMovieResult(
                    posterPath = null,
                    adult = false,
                    overview = "Overview",
                    releaseDate = "2024-01-01",
                    genreIds = emptyList(),
                    id = 1,
                    originalTitle = "Original Title",
                    originalLanguage = "en",
                    title = "Movie Title",
                    backdropPath = null,
                    popularity = 8.0,
                    voteCount = 100,
                    video = false,
                    voteAverage = 8.2,
                ),
            ),
        ),
        MoviesError("Something went wrong"),
    )
}
