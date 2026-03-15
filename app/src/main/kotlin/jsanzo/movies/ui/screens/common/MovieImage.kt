package jsanzo.movies.ui.screens.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.SubcomposeAsyncImage
import jsanzo.movies.R
import jsanzo.movies.ui.PreviewOnDevices

@Composable
internal fun MovieImage(
    posterPath: String,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = posterPath,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator(contentDescription = "")
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
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                )
            }
        },
        modifier = modifier,
    )
}

@PreviewOnDevices
@Composable
private fun MovieImagePreview() {
    MaterialTheme {
        MovieImage("")
    }
}
