package jsanzo.movies.ui.compose.screens.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DetailsScreen(
    movieId: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Details Screen — movieId: $movieId")
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsScreenPreview() {
    DetailsScreen(movieId = 0)
}
