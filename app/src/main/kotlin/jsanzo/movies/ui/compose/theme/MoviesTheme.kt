package jsanzo.movies.ui.compose.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Purple200 = Color(0xFFBB86FC)
private val Purple500 = Color(0xFF6200EE)
private val Purple700 = Color(0xFF3700B3)
private val Teal200 = Color(0xFF03DAC5)
private val Teal700 = Color(0xFF018786)

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = Color.White,
    primaryContainer = Purple700,
    secondary = Teal200,
    onSecondary = Color.Black,
    secondaryContainer = Teal700,
    background = Color.White,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    onPrimary = Color.Black,
    primaryContainer = Purple700,
    secondary = Teal200,
    onSecondary = Color.Black,
    secondaryContainer = Teal700,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun MoviesTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
