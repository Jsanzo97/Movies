package jsanzo.movies.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import jsanzo.movies.R
import jsanzo.movies.presentation.SplashViewModel
import jsanzo.movies.ui.PreviewOnDevices
import jsanzo.movies.ui.theme.MoviesTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToForceUpdate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.trackScreenView()
    }

    LaunchedEffect(state) {
        when (state) {
            is UpToDate -> onNavigateToHome()
            is MustUpdate -> onNavigateToForceUpdate()
            is SplashLoading -> Unit
        }
    }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splash_movies_animation),
    )
    val animationState = animateLottieCompositionAsState(composition = composition)

    if (animationState.isAtEnd && animationState.isPlaying) {
        val version = context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
            ?: "1.0.0"
        viewModel.mustUpdate(version)
    }

    SplashContent(
        composition = composition,
        progress = { animationState.progress },
        modifier = modifier,
    )
}

@Composable
private fun SplashContent(
    composition: LottieComposition?,
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    val appName = stringResource(R.string.application_name)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics {
                contentDescription = appName
                paneTitle = appName
            },
        contentAlignment = Alignment.Center,
    ) {
        LottieAnimation(
            composition = composition,
            progress = progress,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(1f),
        )
    }
}

@PreviewOnDevices
@Composable
private fun SplashScreenPreview() {
    MoviesTheme {
        Surface {
            SplashContent(
                composition = null,
                progress = { 0.5f },
            )
        }
    }
}
