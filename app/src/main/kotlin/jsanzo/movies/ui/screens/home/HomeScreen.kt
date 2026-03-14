package jsanzo.movies.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.skydoves.compose.stability.runtime.IgnoreStabilityReport
import jsanzo.movies.R
import jsanzo.movies.presentation.HomeViewModel
import jsanzo.movies.ui.PreviewOnDevices
import jsanzo.movies.ui.theme.MoviesTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel

private const val ZOOM_OUT_THRESHOLD = 0.9f
private const val ZOOM_IN_THRESHOLD = 1.1f
private const val LAYOUT_TRANSITION_DURATION = 250

@IgnoreStabilityReport
@Composable
fun HomeScreen(
    windowInsets: WindowInsets,
    onNavigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val layoutMode by viewModel.layoutMode.collectAsStateWithLifecycle()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val onSearchQueryChange: (String) -> Unit = {
        searchQuery = it
        viewModel.onSearchQueryChange(it)
    }

    LaunchedEffect(Unit) {
        viewModel.getMovies()
        viewModel.trackScreenView()
    }

    HomeContent(
        state = state,
        layoutMode = layoutMode,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onMovieClick = {
            viewModel.saveMovie(it)
            onNavigateToDetails(it.id)
        },
        onLastVisibleIndex = { viewModel.notifyLastElementVisible(it) },
        onLayoutModeChange = { viewModel.saveLayoutMode(it) },
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(windowInsets),
    )
}

@Composable
private fun HomeContent(
    state: HomeViewState,
    layoutMode: LayoutModeUi,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMovieClick: (MovieUi) -> Unit,
    onLastVisibleIndex: (Int) -> Unit,
    onLayoutModeChange: (LayoutModeUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val appName = stringResource(R.string.application_name)

    Surface(
        modifier = modifier
            .semantics { paneTitle = appName },
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            HomeSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
            )
            Spacer(modifier = Modifier.height(8.dp))
            HomeLayout(
                state = state,
                layoutMode = layoutMode,
                onMovieClick = onMovieClick,
                onLastVisibleIndex = onLastVisibleIndex,
                onLayoutModeChange = onLayoutModeChange,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = searchQuery,
                onQueryChange = { onSearchQueryChange(it) },
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text(stringResource(R.string.home_screen_search_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(R.string.home_screen_search_icon_content_description),
                    )
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        modifier = Modifier.fillMaxWidth(),
        content = {},
    )
}

@Composable
private fun HomeLayout(
    state: HomeViewState,
    layoutMode: LayoutModeUi,
    onMovieClick: (MovieUi) -> Unit,
    onLastVisibleIndex: (Int) -> Unit,
    onLayoutModeChange: (LayoutModeUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listLayoutState = rememberLazyGridState()

    LaunchedEffect(Unit) {
        snapshotFlow {
            listLayoutState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        }.distinctUntilChanged().collect {
            onLastVisibleIndex(it)
        }
    }

    when (state) {
        is Loading -> {
            val loadingMessage = stringResource(R.string.home_screen_loading_movies)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = modifier.semantics { contentDescription = loadingMessage },
                )
            }
        }

        is MovieListComplete -> {
            MoviesLayout(
                movies = state.movies,
                layoutMode = layoutMode,
                layoutState = listLayoutState,
                onMovieClick = onMovieClick,
                onLayoutModeChange = onLayoutModeChange,
            )
        }

        is MoviesSearch -> {
            if (state.movies.isNotEmpty()) {
                MoviesLayout(
                    movies = state.movies,
                    layoutMode = layoutMode,
                    layoutState = LazyGridState(),
                    onMovieClick = onMovieClick,
                    onLayoutModeChange = onLayoutModeChange,
                )
            } else {
                EmptySearch()
            }
        }

        is MoviesError -> {
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun MoviesLayout(
    movies: ImmutableList<MovieUi>,
    layoutMode: LayoutModeUi,
    layoutState: LazyGridState,
    onMovieClick: (MovieUi) -> Unit,
    onLayoutModeChange: (LayoutModeUi) -> Unit,
) {
    AnimatedContent(
        targetState = layoutMode,
        transitionSpec = {
            scaleIn(
                animationSpec = tween(LAYOUT_TRANSITION_DURATION, easing = EaseInOut),
                initialScale = 0.9f,
            ) + fadeIn(tween(LAYOUT_TRANSITION_DURATION)) togetherWith
                scaleOut(
                    animationSpec = tween(LAYOUT_TRANSITION_DURATION, easing = EaseInOut),
                    targetScale = 0.9f,
                ) + fadeOut(tween(LAYOUT_TRANSITION_DURATION))
        },
        label = "layout_mode_transition",
        modifier = Modifier.fillMaxSize(),
    ) { currentLayoutMode ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(currentLayoutMode.columns),
            state = layoutState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .pinchToToggleLayout(currentLayoutMode) { onLayoutModeChange(it) },
        ) {
            items(
                items = movies,
                key = { movie -> movie.id },
            ) { movie ->
                MovieItem(
                    movie = movie,
                    currentLayoutMode = currentLayoutMode,
                    onClick = { onMovieClick(movie) },
                )
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun MovieItem(
    movie: MovieUi,
    currentLayoutMode: LayoutModeUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val movieDescription = stringResource(
        R.string.home_screen_movie_item_description,
        movie.title,
        movie.voteAverage.toString(),
        movie.releaseDate,
    )

    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = movieDescription
            },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SubcomposeAsyncImage(
                model = movie.posterPath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
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
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
            )

            when (currentLayoutMode) {
                LayoutModeUi.Grid2 -> {
                    MovieInfoColumn(
                        movie = movie,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    )
                }

                LayoutModeUi.Grid3 -> {
                    LabeledText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = stringResource(R.string.home_screen_movie_score),
                        value = movie.voteAverage.toString(),
                    )
                }

                LayoutModeUi.Grid4 -> {}
            }
        }
    }
}

@Composable
private fun MovieInfoColumn(
    movie: MovieUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = movie.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        LabeledText(
            label = stringResource(R.string.home_screen_movie_score),
            value = movie.voteAverage.toString(),
        )
        LabeledText(
            label = stringResource(R.string.home_screen_movie_date),
            value = movie.releaseDate,
        )
        LabeledText(
            label = stringResource(R.string.home_screen_movie_language),
            value = movie.originalLanguage.uppercase(),
        )
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
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(4.dp))
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
private fun EmptySearch() {
    val noResultsDescription = stringResource(R.string.home_screen_no_results)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics(mergeDescendants = true) {
                contentDescription = noResultsDescription
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_empty_search),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = noResultsDescription,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun Modifier.pinchToToggleLayout(
    currentMode: LayoutModeUi,
    onLayoutChange: (LayoutModeUi) -> Unit,
): Modifier = pointerInput(currentMode) {
    awaitEachGesture {
        var zoom = 1f
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            if (event.changes.size >= 2) {
                zoom *= event.calculateZoom()
                when {
                    zoom < ZOOM_OUT_THRESHOLD -> {
                        when (currentMode) {
                            LayoutModeUi.Grid2 -> onLayoutChange(LayoutModeUi.Grid3)
                            LayoutModeUi.Grid3 -> onLayoutChange(LayoutModeUi.Grid4)
                            LayoutModeUi.Grid4 -> Unit
                        }
                        zoom = 1f
                    }

                    zoom > ZOOM_IN_THRESHOLD -> {
                        when (currentMode) {
                            LayoutModeUi.Grid4 -> onLayoutChange(LayoutModeUi.Grid3)
                            LayoutModeUi.Grid3 -> onLayoutChange(LayoutModeUi.Grid2)
                            LayoutModeUi.Grid2 -> Unit
                        }
                        zoom = 1f
                    }
                }
                event.changes.forEach { it.consume() }
            }
        } while (event.changes.any { it.pressed })
    }
}

@Composable
@PreviewOnDevices
private fun HomeContentGrid2Preview(
    @PreviewParameter(HomeViewStateProvider::class) state: HomeViewState,
) {
    MoviesTheme {
        HomeContent(
            state = state,
            layoutMode = LayoutModeUi.Grid2,
            searchQuery = "",
            onSearchQueryChange = {},
            onMovieClick = {},
            onLastVisibleIndex = {},
            onLayoutModeChange = {},
        )
    }
}

@Composable
@PreviewOnDevices
private fun HomeContentGrid3Preview(
    @PreviewParameter(HomeViewStateProvider::class) state: HomeViewState,
) {
    MoviesTheme {
        HomeContent(
            state = state,
            layoutMode = LayoutModeUi.Grid3,
            searchQuery = "",
            onSearchQueryChange = {},
            onMovieClick = {},
            onLastVisibleIndex = {},
            onLayoutModeChange = {},
        )
    }
}

@Composable
@PreviewOnDevices
private fun HomeContentGrid4Preview(
    @PreviewParameter(HomeViewStateProvider::class) state: HomeViewState,
) {
    MoviesTheme {
        HomeContent(
            state = state,
            layoutMode = LayoutModeUi.Grid4,
            searchQuery = "",
            onSearchQueryChange = {},
            onMovieClick = {},
            onLastVisibleIndex = {},
            onLayoutModeChange = {},
        )
    }
}

private class HomeViewStateProvider : PreviewParameterProvider<HomeViewState> {
    override val values: Sequence<HomeViewState> = sequenceOf(
        Loading,
        MovieListComplete(
            movies = persistentListOf(
                MovieUi(
                    posterPath = null,
                    adult = false,
                    overview = "Overview",
                    releaseDate = "2024-01-01",
                    genreIds = persistentListOf(),
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
