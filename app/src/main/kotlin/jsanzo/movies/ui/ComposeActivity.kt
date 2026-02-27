package jsanzo.movies.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import jsanzo.movies.BuildConfig
import jsanzo.movies.ui.navigation.AppNavigation
import jsanzo.movies.ui.theme.MoviesTheme

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoviesTheme {
                AppNavigation()
                RequestNotificationPermission()
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun RequestNotificationPermission() {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionState = rememberPermissionState(
                permission = Manifest.permission.POST_NOTIFICATIONS,
            )

            LaunchedEffect(Unit) {
                if (!permissionState.status.isGranted) {
                    permissionState.launchPermissionRequest()
                }
            }
        }
    }
}

@Preview(name = "Small Phone", device = "spec:width=360dp,height=640dp,dpi=480", apiLevel = 36, showBackground = true)
@Preview(name = "Medium Phone", device = "spec:width=411dp,height=891dp,dpi=420", apiLevel = 36, showBackground = true)
@Preview(name = "Large Phone", device = "spec:width=600dp,height=1024dp,dpi=480", apiLevel = 36, showBackground = true)
annotation class PreviewOnDevices
