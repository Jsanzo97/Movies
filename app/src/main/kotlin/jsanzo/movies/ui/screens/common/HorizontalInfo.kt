package jsanzo.movies.ui.screens.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jsanzo.movies.ui.PreviewOnDevices

@Composable
internal fun HorizontalInfo(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .semantics { heading() }
                .alignByBaseline(),
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

@PreviewOnDevices
@Composable
private fun HorizontalInfoPreview() {
    MaterialTheme {
        HorizontalInfo(
            label = "Label",
            value = "Value",
        )
    }
}
