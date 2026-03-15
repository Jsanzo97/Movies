package jsanzo.movies.ui.screens.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import jsanzo.movies.ui.PreviewOnDevices

@Composable
internal fun LoadingIndicator(
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics {
                this.contentDescription = contentDescription
            },
        )
    }
}

@PreviewOnDevices
@Composable
private fun LoadingIndicatorPreview() {
    MaterialTheme {
        LoadingIndicator("")
    }
}
