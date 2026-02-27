package jsanzo.movies.ui.screens.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import jsanzo.movies.ui.PreviewOnDevices

@Composable
fun ForceUpdateScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Text("Force update")
    }
}

@Composable
@PreviewOnDevices
private fun ForceUpdateScreenPreview() {
    ForceUpdateScreen()
}
