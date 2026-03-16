# agents.md — Movies Android Project

> **Maintenance**: This file should be updated automatically whenever relevant changes occur in the project — new libraries, migrations, plugins, architectural decisions, or structural changes. Also update it when explicitly requested.

---

## Project Overview

Multi-module Android application built with Kotlin that displays movies using The Movie Database (TMDB) API. Follows Clean Architecture with MVVM presentation pattern. Fully migrated to Jetpack Compose and Navigation3. Tests fully migrated to JUnit 5.

---

## Tech Stack

| Area                    | Technology                           | Notes                                                                                            |
|-------------------------|--------------------------------------|--------------------------------------------------------------------------------------------------|
| Language                | Kotlin 2.3.10                        |                                                                                                  |
| UI                      | Jetpack Compose                      | Migration from XML/Fragments complete                                                            |
| Architecture            | MVVM + Clean Architecture            |                                                                                                  |
| DI                      | Koin 4.1.1                           | Using Koin Annotations for DI                                                                    |
| Navigation              | Navigation3 1.0.1                    | Migrated from Navigation Compose. `NavDisplay` + `rememberNavBackStack`                          |
| Networking              | Retrofit 3.0.0 + OkHttp 5.3.2        |                                                                                                  |
| Serialization           | Kotlinx Serialization 1.10.0         | Applied via CommonSetupPlugin to all modules                                                     |
| Database                | Room 2.8.4                           |                                                                                                  |
| Async                   | Coroutines 1.10.2 + Flow + StateFlow |                                                                                                  |
| Image loading           | Coil 2.7.0                           | Replaced Glide                                                                                   |
| Error handling          | Arrow 2.2.2  (Either, Option)        |
| Analytics & Crashlytics | Firebase BOM 34.10.0                 | Crashlytics + Analytics + Remote Config with DebugView                                           |
| Splash + ForceUpdate    | Lottie 6.7.1                         | Animated splash screen + force update screen with JSON animations                                |
| HTTP inspector          | Chucker                              | debugImplementation only, no-op in release                                                       |
| Performance             | Develocity 4.3.2                     | Build scans, build cache and performance insights                                                |
| Memory Leaks            | LeakCanary 2.14                      | Automated detection in debug builds                                                              |
| Compose stability       | compose-stability-analyzer 0.7.0     | Gradle plugin + IDE plugin. Gradle task: `stabilityCheck`. Baseline commited in `app/stability/` |
| Static analysis         | Detekt 1.23.8                        |                                                                                                  |
| Build system            | Gradle 9.3.1 (Kotlin DSL)            |                                                                                                  |
| Min SDK                 | 26                                   |                                                                                                  |
| Target/Compile SDK      | 36                                   |                                                                                                  |
| Java compatibility      | Java 21                              |                                                                                                  |
| Automation              | Fastlane                             | CD and Play Store metadata management                                                            |

---

## Module Structure

```
Movies/
├── app/             # Application module (com.android.application)
├── domain/          # UseCases, Entities, Repository interfaces, Errors
├── data/            # Repository implementations, Data entities
├── datastore/       # DataStore Preferences implementation (LayoutMode persistence)
├── database/        # Room database, DAOs, DB entities
├── remote/          # Retrofit API, Remote DTOs, Remote data sources
└── build-logic/     # Convention plugins (Gradle build logic)
```

### Dependency Graph

```
app       → domain, data, datastore, database, remote
data      → domain, database, remote
datastore → data (implements DataStoreStorage defined in :data)
remote    → domain (for interfaces)
database  → domain (for interfaces)
domain    → (no dependencies)
```

---

## Package Naming Convention

Base package: `jsanzo.movies`

Each module appends its name automatically via `calculateNamespace()` in build-logic:
- `:domain` → `jsanzo.movies.domain`
- `:data` → `jsanzo.movies.data`
- `:datastore` → `jsanzo.movies.datastore`
- `:database` → `jsanzo.movies.database`
- `:remote` → `jsanzo.movies.remote`
- `:app` → `jsanzo.movies` (special case — root app module)

---

## Architecture

### Domain Layer (`:domain`)
- **Entities**: Pure Kotlin data classes (`Movie`, `MovieDetails`, `MovieResult`)
- **UseCases**: Single-responsibility, `suspend operator fun invoke()` pattern
- **Repository/Datastore interfaces**: Defined here, implemented in outer layers (`:data`, `:remote`, `:database`)
- **Errors**: Sealed classes/objects (`InvalidParametersError`, `NotFoundError`)
- Zero Android dependencies

#### UseCase pattern
```kotlin
class UseCase(private val repository: Repository) {
  suspend operator fun invoke() = repository.function()
}
```

#### Arrow extension functions (defined in `:domain`)
All Arrow extension functions are `suspend` to allow calling suspend functions inside their lambdas:
```kotlin
suspend fun <L, R> Either<L, R>.onSuccess(action: suspend (R) -> Unit): Either<L, R>
suspend fun <L, R> Either<L, R>.onError(action: suspend (L) -> Unit): Either<L, R>
suspend fun <T> Option<T>.onSome(action: suspend (T) -> Unit): Option<T>
suspend fun <T> Option<T>.onNone(action: suspend () -> Unit): Option<T>
```

### Data Layer (`:data`)
- Repository implementations orchestrating `:remote` and `:database` datastores.
- Own data entities with extension functions for mapping.

#### Mapping pattern (extension functions, no dedicated mapper classes)
```kotlin
fun DataMovieResult.toMovieResult() = MovieResult()
fun MovieResult.toDataMovieResult() = DataMovieResult()
```
> Note: Dedicated mapper classes may be introduced in a future refactor.

### Remote Layer (`:remote`)
- Retrofit API interface.
- Remote DTOs mapped to domain entities via extension functions.
- Base URL: `https://api.themoviedb.org/3/movie/`, available via `BuildConfig.SERVER_ENDPOINT`.
- API Key available via `BuildConfig.SERVER_API_KEY`.

### Database Layer (`:database`)
- Room database with KSP.
- DAOs for local persistence.
- DB entities mapped to domain entities via extension functions.

### DataStore Layer (`:datastore`)
- Implements `DataStoreStorage` interface defined in `:data`.
- Persists user preferences using `androidx.datastore:datastore-preferences`.
- Single preferences file: `movies_preferences`.
- `DataStoreStorageImpl` stores enum values as their name string and reads them back with a default fallback.
- `DataStoreModule` (Koin) wires `DataStoreStorageImpl` as the `DataStoreStorage` singleton.

#### DataStore architecture flow
```
domain: DomainLayoutModePreference, DataStoreRepository (interface), GetLayoutModeUseCase, SaveLayoutModeUseCase
data:   DataStoreStorage (interface), DataStoreRepositoryImpl
datastore: DataStoreStorageImpl, DataStoreModule
app:    AppModule includes DataStoreModule
```

#### Key stored values
| Key | Type | Default |
|---|---|---|
| `layout_mode` | String (enum name) | `Grid2` |

### Presentation Layer (`:app`)
- ViewModels live in `presentation/` package (separated from UI screens).
- ViewModels expose `StateFlow<ViewState>`.
- Sealed classes for ViewState per screen, located in `ui/screens/`.

---

## Compose Setup

Compose is configured in `SetupAndroidApplicationPlugin` (`:app` only). Key decisions:

### Plugin setup
- `buildFeatures.compose = true` is set inside `SetupAndroidApplicationPlugin`
- The `org.jetbrains.kotlin.plugin.compose` plugin is declared with `apply false` in the **root** `build.gradle.kts` to make it available on the classpath without triggering classpath conflicts
- It is then applied explicitly in `app/build.gradle.kts` via `alias(libs.plugins.compose.compiler)`

> **Why not apply it from the convention plugin?** Applying it programmatically via `pluginManager.apply()` from `build-logic` causes `org.jetbrains:annotations` version conflicts with AGP 9.0.1 + Kotlin 2.x embedded in Gradle. The `apply false` in root + explicit apply in `:app` is the correct workaround.

### Compose dependencies (added in `SetupAndroidApplicationPlugin`)
```kotlin
"implementation"(platform(libs().getLibrary("compose-bom")))
"implementation"(libs().getLibrary("compose-ui"))
"implementation"(libs().getLibrary("compose-material3"))
"implementation"(libs().getLibrary("compose-ui-tooling-preview"))
"implementation"(libs().getLibrary("androidx-navigation3-ui"))
"implementation"(libs().getLibrary("androidx-navigation3-runtime"))
"implementation"(libs().getLibrary("androidx-lifecycle-runtime-compose"))
"implementation"(libs().getLibrary("koin-compose"))
"implementation"(libs().getLibrary("coil-compose"))
"implementation"(libs().getLibrary("compose-material-icons-extended"))
"implementation"(libs().getLibrary("accompanist-permissions"))
"implementation"(libs().getLibrary("lottie-compose"))
"implementation"(libs().getLibrary("kotlinx-coroutines-play-services"))
"debugImplementation"(libs().getLibrary("compose-ui-tooling"))
```

### Compose screen structure in `:app`
```
app/src/main/kotlin/jsanzo/movies/
├── di/
│   └── AppModule.kt
├── presentation/
│   ├── HomeViewModel.kt
│   ├── DetailsViewModel.kt
│   └── SplashViewModel.kt
├── ui/
│   ├── navigation/
│   │   ├── AppDestinations.kt
│   │   └── AppNavigation.kt
│   └── screens/
│       ├── splash/
│       │   ├── SplashScreen.kt
│       │   └── SplashViewState.kt
│       ├── home/
│       │   ├── HomeScreen.kt
│       │   └── HomeViewState.kt
│       ├── details/
│       │   ├── DetailsScreen.kt
│       │   └── DetailsViewState.kt
│       └── forceupdate/
│           └── ForceUpdateScreen.kt
├── theme/
│   └── MoviesTheme.kt
├── tracking/
│   ├── MovieTracker.kt
│   └── FirebaseTracker.kt
├── ComposeActivity.kt
├── Constants.kt
└── MoviesApplication.kt
```

### Compose Previews

All screens use a custom `@PreviewOnDevices` multipreview annotation that covers small, medium and large devices with LTR and RTL support, all at API level 36:

```kotlin
@Preview(name = "Small Phone", device = "spec:width=360dp,height=640dp,dpi=480", apiLevel = 36, showBackground = true)
@Preview(name = "Medium Phone LTR", device = "spec:width=411dp,height=891dp,dpi=420", apiLevel = 36, showBackground = true, locale = "es")
@Preview(name = "Medium Phone RTL", device = "spec:width=411dp,height=891dp,dpi=420", apiLevel = 36, showBackground = true, locale = "ar")
@Preview(name = "Large Phone", device = "spec:width=600dp,height=1024dp,dpi=480", apiLevel = 36, showBackground = true)
annotation class PreviewOnDevices
```

Screens split their composable into a `Screen` (owns ViewModel and state) and a `Content` (pure composable receiving only plain parameters) so `@PreviewOnDevices` can be applied to `Content` without needing a ViewModel or context. Nullable parameters like `LottieComposition` are passed as `null` in previews to show a static midpoint state.

### Navigation
Type-safe Navigation3 using `NavKey` + `@Serializable` data objects/classes:
```kotlin
sealed interface AppDestinations : NavKey {
  @Serializable data object Splash : AppDestinations
  @Serializable data object Home : AppDestinations
  @Serializable data object ForceUpdate : AppDestinations
  @Serializable data class Details(val movieId: Int) : AppDestinations
}
```

Navigation is managed via `rememberNavBackStack` and `NavDisplay`. No `NavController` — forward navigation uses `backStack.add()`, back navigation uses `backStack.removeLastOrNull()`.

Slide animations configured via `transitionSpec` (left→right on navigate) and `popTransitionSpec` (right→left on back). The Splash→Home and Splash→ForceUpdate transitions use `fadeIn/fadeOut`.

`android:enableOnBackInvokedCallback="true"` is set in `AndroidManifest.xml` on the `<application>` tag. Without this flag, Android 14+ intercepts the back gesture at the system level before Nav3 can handle it, causing `popTransitionSpec` and `predictivePopTransitionSpec` to never fire.

`ComposeActivity` is the sole launcher Activity. All XML navigation, Fragments, Safe Args, and related dependencies have been removed.

#### RTL support in navigation animations
`AppNavigation` reads `LocalLayoutDirection` to detect RTL locales and adjusts slide animation directions accordingly via a `directionMultiplier`:
```kotlin
val layoutDirection = LocalLayoutDirection.current
val isRtl = layoutDirection == LayoutDirection.Rtl
// isRtl is captured outside the lambda — LocalLayoutDirection is a CompositionLocal
// and transitionSpec lambdas execute outside composition, so the value must be captured beforehand.
val directionMultiplier = if (isRtl) -1 else 1
```
This value is reused in `transitionSpec`, `popTransitionSpec`, and `predictivePopTransitionSpec` so all slide directions are consistently mirrored in RTL.

---

## Theme

`MoviesTheme` in `ui/theme/MoviesTheme.kt` defines a full Material 3 color scheme generated from seed color `#1B4B8A` (navy blue). Supports Dynamic Color on Android 12+ (API 31+), falling back to the hardcoded M3 palette on older versions. Respects system dark mode via `isSystemInDarkTheme()`.

### Color seed
`#1B4B8A` — navy blue, neutral and professional, lets movie posters be the visual focus.

### Dynamic Color
On Android 12+ (`Build.VERSION_CODES.S`), the color scheme adapts to the user's wallpaper via `dynamicLightColorScheme` / `dynamicDarkColorScheme`. On older versions the hardcoded M3 palette is used.

### Typography
Full M3 typography scale defined explicitly in `AppTypography` — all 15 text styles from `displayLarge` to `labelSmall` with proper weights and line heights.

### Window insets
`WindowCompat.setDecorFitsSystemWindows(window, false)` set in `MoviesTheme` so content draws edge-to-edge. Status bar and navigation bar icon colors adapt to dark/light theme via `isAppearanceLightStatusBars` and `isAppearanceLightNavigationBars`.

`ComposeActivity` is the sole launcher Activity, configured with `android:theme="@style/android:Theme.Material.Light.NoActionBar"` to avoid the default action bar. Any translucent window properties were removed to ensure standard system behaviors like screen rotation are not restricted. After Compose is ready, the theme is effectively replaced by `MoviesTheme`.

---

## Accessibility

All screens implement Compose semantics for TalkBack and other assistive technologies:

### Conventions applied across screens
- `paneTitle` on the root `Surface` of every screen — TalkBack announces the screen name on navigation.
- `heading()` on title/section header `Text` composables — allows users to navigate by headings.
- `mergeDescendants = true` on composite elements (e.g. `InfoChip`, movie cards) — TalkBack reads them as a single unit.
- `contentDescription` on loading indicators and icon-only elements.
- `Role.Button` on clickable non-button elements (e.g. the homepage link in `DetailsScreen`).
- Images that are decorative when adjacent to a title use `contentDescription = null`.

### Screen-specific notes

**HomeScreen**
- `paneTitle` set to the app name on the root `Surface`.
- `MovieItem` card uses `mergeDescendants = true` with a formatted `contentDescription` combining title, score and release date — TalkBack reads the full card as one announcement.
- Loading indicator has an explicit `contentDescription`.
- Empty search state uses `mergeDescendants = true` on the `Column` with a `contentDescription` matching the no-results string — the decorative icon uses `contentDescription = null`.

**DetailsScreen**
- `paneTitle` is dynamic: shows the movie title when in `DetailsSuccess` state, falls back to a generic string otherwise.
- Movie title `Text` uses `heading()`.
- Section titles in `DetailSection` use `heading()`.
- `InfoChip` uses `mergeDescendants = true` so label and value are read together.
- Homepage link has `contentDescription` combining a localised prefix with the URL, and `Role.Button`.
- Poster image uses `contentDescription = null` (decorative, title is adjacent).

**SplashScreen**
- Root `Box` has both `contentDescription` and `paneTitle` set to the app name.

**ForceUpdateScreen**
- Root `Surface` has `paneTitle` set to the update title string.
- Update title `Text` uses `heading()`.
- Center `Column` uses `mergeDescendants = true`.

---

## ViewState pattern

Each screen defines its own sealed class in `ScreenViewState.kt`:
- `Loading` — data object, shown while fetching
- `ScreenSuccess` — data class with `@Immutable`, holds domain model
- `ScreenError` — data class with `@Immutable`, holds error message string

Navigation side effects (e.g. navigate to details) are handled via `SharedFlow<T>` instead of ViewState.

---

## Window Insets

All screens use `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)` on the root composable to handle both status bar and navigation bar (buttons or gestures) correctly.

---

## Dependency Injection — Koin Annotations

The project uses Koin Annotations for dependency injection. Each Gradle module is responsible for its own dependency providers. `@KoinViewModel` is used for ViewModels, with `@ComponentScan` for automatic scanning.

Koin is initialized in `MoviesApplication.onCreate()` by loading a single, aggregated `AppModule`.

### StrictMode

StrictMode is enabled in debug builds only in `MoviesApplication`:

```kotlin
private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

override fun onCreate() {
    super.onCreate()
    setupStrictMode()
    startKoin { ... }
}

private fun setupStrictMode() {
    if (BuildConfig.DEBUG) {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork().detectCustomSlowCalls()
                .penaltyLog().build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build()
        )
    }
}
```

`detectUntaggedSockets()` is intentionally excluded from `ThreadPolicy` — Firebase/OkHttp background threads trigger it and it is not actionable.

**Known acceptable violations:**
- `UntaggedSocketViolation` — Firebase Crashlytics sending reports on its own thread. Library code, not actionable.
- `LeakedClosableViolation` in `UnixSecureDirectoryStream` — triggered by the Android GC finalizer daemon. Not actionable.

### Koin Module Structure

| Koin Module | Location | Purpose |
|---|---|---|
| `AppModule` | `:app/di` | Main module, includes all other modules. |
| `DataModule` | `:data/di` | Provides repository implementations. |
| `DataStoreModule`| `:datastore/di`| Provides `DataStoreStorageImpl` as `DataStoreStorage`. |
| `DatabaseModule`| `:database/di`| Provides Room DB, DAOs, and the local datastore. |
| `RemoteModule` | `:remote/di` | Provides the remote datastore (`MoviesService`) and `FirebaseRemoteConfig`. |
| `NetworkModule`| `:remote/di` | Provides Retrofit and OkHttp dependencies. |
| `AppRemoteModule`| `:remote/di/`| Provides build-variant specific network config (e.g., Chucker). |
| `DomainModule` | `:domain/di` | Provides UseCases. |

---

## Firebase

### Setup
- `google-services.json` is gitignored — never committed to the repo
- Two apps registered in Firebase Console: `jsanzo.movies` (release) and `jsanzo.movies.debug` (debug)
- `google-services.json` is passed to CI via GitHub Secret `GOOGLE_SERVICES_JSON` (base64 encoded) and decoded before the build:
```yaml
- name: Decode google-services.json
  run: echo "${{ secrets.GOOGLE_SERVICES_JSON }}" | base64 --decode > app/google-services.json
```
- To encode locally for the secret: `certutil -encode app\google-services.json google-services-b64.txt` (Windows)
- Plugins applied in `app/build.gradle.kts` (not in convention plugin to avoid classpath conflicts):
```kotlin
alias(libs.plugins.google.services)
alias(libs.plugins.firebase.crashlytics)
```
- Dependencies added in `SetupAndroidApplicationPlugin`:
```kotlin
"implementation"(platform(libs().getLibrary("firebase-bom")))
"implementation"(libs().getLibrary("firebase-analytics"))
"implementation"(libs().getLibrary("firebase-crashlytics"))
```
- Firebase Remote Config dependency added in `:remote/build.gradle.kts`:
```kotlin
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.config)
```

### Tracker interface
`MovieTracker` is an interface defined in `:app/tracking/MovieTracker.kt`. `FirebaseTracker` implements it. ViewModels depend on `MovieTracker` not `FirebaseTracker`, making them testable without Firebase:

```kotlin
interface MovieTracker {
  fun trackHomeShown()
  fun trackDetailsShown(movieId: Int)
  fun trackMovieClicked(movieId: Int, movieTitle: String)
  fun trackErrorShown(screen: String, error: String)
  fun trackPageLoaded(page: Int)
  fun trackSplashShown()
  fun trackForceUpdateShown(currentVersion: String)
  fun trackRemoteConfigError()
  fun trackSearchPerformed(query: String, resultsCount: Int)
  fun trackLayoutModeChanged(mode: String)
}
```

In tests, `MovieTracker` is mocked with `mockk(relaxed = true)` so all tracking calls are ignored automatically.

### FirebaseTracker
Located in `:app/tracking/FirebaseTracker.kt`. Single source of truth for all analytics events. Provided via `AppModule`:

```kotlin
@Single
fun provideFirebaseTracker(androidContext: Application): FirebaseTracker =
  FirebaseTracker(
    analytics = FirebaseAnalytics.getInstance(androidContext),
  )
```

### Tracked events

| Function | Event name | Parameters |
|---|---|---|
| `trackHomeShown()` | `screen_view` | `screen_name: "home"` |
| `trackDetailsShown(movieId)` | `screen_view` | `screen_name: "details"`, `movie_id` |
| `trackMovieClicked(movieId, movieTitle)` | `movie_clicked` | `movie_id`, `movie_title` |
| `trackErrorShown(screen, error)` | `error_shown` | `screen`, `error` |
| `trackPageLoaded(page)` | `page_loaded` | `page` |
| `trackSplashShown()` | `screen_view` | `screen_name: "splash"` |
| `trackForceUpdateShown(currentVersion)` | `force_update_shown` | `current_version` |
| `trackRemoteConfigError()` | `remote_config_error` | — |
| `trackSearchPerformed(query, resultsCount)` | `search_performed` | `query`, `results_count` |
| `trackLayoutModeChanged(mode)` | `layout_mode_changed` | `mode` |

### Where events are triggered
- `trackHomeShown()` — `HomeViewModel.trackScreenView()` called from `HomeScreen` `LaunchedEffect`
- `trackDetailsShown()` — `DetailsViewModel.trackScreenView(movieId)` called from `DetailsScreen` `LaunchedEffect`
- `trackMovieClicked()` — `HomeViewModel.saveMovie()`
- `trackErrorShown()` — `HomeViewModel.getMovies()`, `HomeViewModel.searchMovies()` and `DetailsViewModel.getDetails()` on error
- `trackPageLoaded()` — `HomeViewModel.getMovies()` on success
- `trackSplashShown()` — `SplashViewModel.trackScreenView()` called from `SplashScreen` `LaunchedEffect`
- `trackForceUpdateShown()` — `SplashViewModel.mustUpdate()` when `mustUpdate = true`
- `trackRemoteConfigError()` — `SplashViewModel.mustUpdate()` on error
- `trackSearchPerformed()` — `HomeViewModel.searchMovies()` on success, with query and results count
- `trackLayoutModeChanged()` — `HomeViewModel.saveLayoutMode()` called directly (not in a coroutine) before persisting via `SaveLayoutModeUseCase`

### Pending Firebase functions (not yet implemented)
- `setUserId(userId)` — for when user login is added
- `logError(throwable, message)` — for recording handled errors in Crashlytics without crashing

### Remote Config
Firebase Remote Config is used to enforce a minimum app version. On every launch, before navigating to Home, the app fetches the `min_version` key from Remote Config and compares it against the current app version.

**Key:** `min_version`
**Value format:** `{ "latestVersionAvailable": "1.0.0" }`

If the current app version is lower than `minVersion`, the user is redirected to `ForceUpdateScreen` and cannot proceed.

**Configuration:**
- `minimumFetchIntervalInSeconds = 0` — no caching, always fetches fresh values
- `fetchAndActivate().await()` called before reading any value — ensures the latest value is always used

**Flow:**
```
SplashScreen (Lottie animation)
    ↓ animation ends
SplashViewModel.mustUpdate(currentVersion)
    ↓ fetches Remote Config via MustUpdateUseCase
    ├── mustUpdate = true  → ForceUpdateScreen (no back navigation)
    └── mustUpdate = false → HomeScreen
```

**Error handling:** if Remote Config fetch fails, the user is let through to Home — fail open strategy.

**`MustUpdateUseCase`** compares semantic versions (major.minor.patch) and returns `true` if the current version is strictly lower than `minVersion`.

**Koin provider** (`RemoteModule`):
```kotlin
@Single
fun firebaseRemoteConfig(): FirebaseRemoteConfig {
  val remoteConfig = FirebaseRemoteConfig.getInstance()
  remoteConfig.setConfigSettingsAsync(remoteConfigSettings {
    minimumFetchIntervalInSeconds = 0
  })
  return remoteConfig
}
```

### DebugView
Enable/disable Firebase Analytics DebugView via Gradle tasks (defined in `app/build.gradle.kts`):
```bash
./gradlew enableFirebaseDebug   # enables DebugView on connected device
./gradlew disableFirebaseDebug  # disables DebugView
```
> DebugView works reliably on physical devices. Emulators may not appear as debug devices in Firebase Console.

---

## Chucker

HTTP inspector for debug builds only.

```kotlin
debugImplementation(chucker)
releaseImplementation(chucker-no-op)
```

Notification permission is requested only on debug builds and only on Android 13+:
```kotlin
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
```

---

## Splash Screen

### Overview
Animated splash screen using Lottie. No SplashScreen API — used to apply a translucent window theme (`Theme.Movies.Splash`) which has been removed to avoid restrictions on screen rotation.

The animation file is at `app/src/main/res/raw/splash_movies_animation.json`.

### ViewState
```kotlin
sealed class SplashViewState
data object SplashLoading : SplashViewState()
data object MustUpdate : SplashViewState()
data object UpToDate : SplashViewState()
```

### Screen / Content split
`SplashScreen` owns the ViewModel, state collection, version check trigger, and navigation callbacks. It loads `LottieComposition` once via `rememberLottieComposition` and passes it down to `SplashContent` to avoid loading it twice. `SplashContent` is a pure composable receiving `composition` and `progress` — making it previewable without ViewModel or context (pass `composition = null` in the preview).

```kotlin
@Composable
fun SplashScreen(
  onNavigateToHome: () -> Unit,
  onNavigateToForceUpdate: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SplashViewModel = koinViewModel(),
)

@Composable
private fun SplashContent(
  composition: LottieComposition?,
  progress: () -> Float,
  modifier: Modifier = Modifier,
)
```

### Flow
1. Lottie animation plays once (occupies 50% of screen width, `aspectRatio(1f)`)
2. When `animationState.isAtEnd && animationState.isPlaying` → calls `viewModel.mustUpdate(version)`
3. Version read from `context.packageManager.getPackageInfo().versionName`
4. ViewModel fetches Remote Config via `MustUpdateUseCase`
5. State transitions to `MustUpdate` or `UpToDate` → navigation triggered via `LaunchedEffect(state)`

### Key decisions
- Animation sized via `fillMaxWidth(0.5f)` + `aspectRatio(1f)`
- `LottieComposition` loaded once in `SplashScreen` and passed to `SplashContent` — avoids double loading
- On Remote Config error → fail open, navigate to Home
- Preview uses `SplashContent(composition = null, progress = { 0.5f })` to show animation at midpoint without needing ViewModel

---

## Force Update Screen

Screen shown when the app version is below `minVersion` from Remote Config. The user cannot navigate back — `Splash` is removed from the backstack before `ForceUpdate` is added.

### Design
- Lottie animation looping infinitely (`LottieConstants.IterateForever`) centered on screen, sized at `fillMaxWidth(0.8f)` + `aspectRatio(1f)`
- Animation file: `app/src/main/res/raw/force_update_animation.json` (rocket animation themed to app colors)
- Headline and body text centered below the animation
- "Actualizar" `Button` anchored to `Alignment.BottomCenter`

### Screen / Content split
`ForceUpdateScreen` owns the ViewModel and opens the Play Store via `LocalUriHandler`. `ForceUpdateContent` is a pure composable receiving no external state — previewable without context.

```kotlin
@Composable
fun ForceUpdateScreen(modifier: Modifier = Modifier) {
  val uriHandler = LocalUriHandler.current
  ForceUpdateContent(
    onUpdateClick = { runCatching { uriHandler.openUri(PLAY_STORE_URL) } },
    modifier = modifier,
  )
}
```

Play Store URL: `market://details?id=jsanzo.movies`

### Key decisions
- `runCatching` around `openUri` prevents crash if Play Store is not installed on the device
- `windowInsetsPadding(WindowInsets.safeDrawing)` on root `Box`
- `Surface` as root with `colorScheme.background`

---

## Home Screen

### ViewState
```kotlin
@Stable
sealed class HomeViewState
data object Loading : HomeViewState()

@Immutable
data class MoviesSuccess(val movies: ImmutableList<MovieUi>) : HomeViewState()

@Immutable
data class MoviesError(val message: String) : HomeViewState()
```

Navigation to details is handled as a side effect via `SharedFlow<Int>` instead of a ViewState.

### UI Models — `MovieUi` / `MovieDetailsUi`

`MovieUi` is an `@Immutable data class` with `ImmutableList<Int>` for `genreIds` (from `kotlinx-collections-immutable`). `MovieDetailsUi` is an `@Immutable data class` with genre and production company lists flattened to comma-separated `String` values. Both are mapped from domain entities via extension functions in `MovieUiMapper.kt` / `MovieDetailsUiMapper.kt`.

### Layout Mode

The home screen supports multiple grid layouts (`Grid2`, `Grid3`, `Grid4`). The active layout is persisted in DataStore and exposed as `StateFlow<LayoutModeUi>` from `HomeViewModel`.

```kotlin
// HomeViewModel
val layoutMode: StateFlow<LayoutModeUi> = getLayoutModeUseCase()
  .map { it.toUi() }
  .stateIn(viewModelScope, WhileSubscribed(5000), LayoutModeUi.Grid2)

internal fun saveLayoutMode(mode: LayoutModeUi) {
  firebaseTracker.trackLayoutModeChanged(mode.name)  // called directly, not in coroutine
  viewModelScope.launch { saveLayoutModeUseCase(mode.toDomainLayoutModePreference()) }
}
```

The UI uses `AnimatedContent` to animate transitions between layouts.

### Search
The `SearchBar` calls `viewModel.onSearchQueryChange(query)` on every keystroke. The ViewModel holds a private `MutableSharedFlow<String>` observed in `init` with `debounce(500ms)` + `distinctUntilChanged`. When the debounce fires:
- **Query blank** → restores `moviesRetrieved` in state as `MovieListComplete` without any API call
- **Query not blank** → calls `SearchMoviesUseCase`

The search result is not cached — every query hits the API fresh. On network failure the local fallback searches by title in the Room database (`LIKE '%query%'`) over the movies the user has previously visited in details.

### ViewModel key decisions
- `isLoadingPage` flag to prevent duplicate page requests during fast scroll:
```kotlin
private var isLoadingPage = false

fun getMovies() {
    if (moviesRetrieved.isEmpty()) {
        loadPage(nextPageToRetrieve)
    }
}

private fun checkNeedNewPage() {
    if (_state.value is MovieListComplete && lastVisible + PAGINATION_THRESHOLD >= moviesRetrieved.size) {
        loadPage(nextPageToRetrieve)
    }
}

private fun loadPage(page: Int) {
    if (!isLoadingPage) {
        viewModelScope.launch {
            isLoadingPage = true
            // ...
            isLoadingPage = false
        }
    }
}
```

`getMovies()` is the public screen-facing call — only loads if no data. This prevents the `LaunchedEffect(Unit)` in `HomeScreen` from re-triggering a full load when navigating back from Details, which would overwrite a `MoviesSearch` state with `MovieListComplete`. `loadPage()` is internal, called by pagination via `checkNeedNewPage()` without the empty check.
- Deduplication using `Set` of IDs: `moviesRetrieved.map { it.id }.toSet()`
- Pagination based on `moviesRetrieved.size` (not a separate counter) to account for filtered duplicates
- `nextPageToRetrieve` incremented only on `onSuccess`
- Loading state only emitted on page 1 to avoid hiding the list during pagination

### Pagination logic
```kotlin
private fun checkNeedNewPage() {
  if (_state.value is MovieListComplete && lastVisible + PAGINATION_THRESHOLD >= moviesRetrieved.size) {
    loadPage(nextPageToRetrieve)
  }
}
```

### UI key decisions
- `SearchBar` (Material 3) at top — API search via ViewModel, no local filtering
- `Box` content uses a `when` over `state` — `Loading`, `MoviesSuccess` and `MoviesError` are each their own branch
- Within `MoviesSuccess`: list shown if `movies.isNotEmpty()`, empty state shown if `movies.isEmpty() && searchQuery.isNotBlank()`
- Empty search state: centered `Column` with `ic_empty_search` icon (`tint = colorScheme.primary`, `contentDescription = null`) + `Text`. The `Column` uses `mergeDescendants = true` with a `contentDescription` matching the no-results string for TalkBack
- `ic_empty_search` vector uses `fillColor = "#FFFFFF"` — color is fully controlled by the `tint` parameter in Compose
- `movies` read directly from `MoviesSuccess.movies` — no `remember` filtering
- `SubcomposeAsyncImage` with loading indicator and error fallback (`ic_error_load`)
- `@Stable` / `@Immutable` annotations on ViewState for Compose stability
- `key = { _, movie -> movie.id }` in `itemsIndexed` to prevent duplicate key crashes
- `windowInsetsPadding(WindowInsets.statusBars)` on root Column
- `Surface` as root container with `colorScheme.background`
- Movie cards: `RoundedCornerShape(12.dp)` on images, `titleSmall` bold for title, `bodySmall` + `onSurfaceVariant` for labels
- Error state uses `colorScheme.error`

---

## Details Screen

### ViewState
```kotlin
@Stable
sealed class DetailsViewState
data object Loading : DetailsViewState()

@Immutable
data class DetailsSuccess(val movieDetails: DomainMovieDetails) : DetailsViewState()

@Immutable
data class DetailsError(val message: String) : DetailsViewState()
```

### UI key decisions
- `LaunchedEffect(movieId)` triggers `viewModel.getDetails(movieId)` once on entry
- `Surface` wrapping `DetailsScreenContent` with `colorScheme.background` to prevent dark mode text rendering issues
- Layout: top `Row` with poster image (130dp wide, 195dp tall, `RoundedCornerShape(12.dp)`) + basic info column; remaining fields in full-width sections below
- `SubcomposeAsyncImage` with loading indicator and error fallback (`ic_error_load`)
- `verticalScroll` on root `Column` for long content
- `windowInsetsPadding(WindowInsets.safeDrawing)` on root Column
- `runtime` and `homepage` are nullable — only rendered if present
- `homepage` is clickable via `LocalUriHandler` — opens system browser, styled with `colorScheme.primary` and `TextDecoration.Underline`
- `tagline` shown below title in `onSurfaceVariant` if present
- `HorizontalDivider` separates header from body sections
- `DetailSection` composable for body fields: `labelSmall` bold in `onSurfaceVariant` + `bodyMedium` value
- `InfoChip` composable for header fields: `bodySmall` bold in `onSurfaceVariant` + `bodySmall` value
- Status and Revenue displayed side by side as two columns
- `BASE_IMAGE_URL_ORIGINAL` defined as private constant in `DetailsScreen.kt`
- Formatting helpers (`formatLanguages`, `formatGenres`, etc.) are private functions using `joinToString(", ")`
- Error state uses `colorScheme.error`

---

## Build Logic (`build-logic` module)

Convention plugins are defined in `build-logic/src/main/kotlin/`.

### Available Plugins
| Plugin ID | Class | Purpose |
|---|---|---|
| `setup-android-application` | `SetupAndroidApplicationPlugin` | Base setup for `:app`. Includes Compose config and dependencies. |
| `setup-android-library` | `SetupAndroidLibraryPlugin` | Base setup for Android library modules. |
| `common-setup` | `CommonSetupPlugin` | Applies Detekt, Spotless, Serialization, Koin, JUnit and other common configurations. |

### CommonSetupPlugin responsibilities
- `setupDetekt()` — applies Detekt plugin and `detektPlugins(detekt-rules-compose)`
- `setupSpotless()` — applies KtLint via Spotless
- `setupCheck()` — wires `check` task to Detekt and Spotless
- `setupJunitTests()` — configures JUnit 5
- `setupSerialization()` — applies `org.jetbrains.kotlin.plugin.serialization` to all modules
- `setupKoin()` — configures Koin
- `setupJacocoReport()` — configures JaCoCo 0.8.12, registers `jacocoDebugTestReport` task per module, exposes excludes list via `project.extra["jacocoExcludes"]`

---

## Compose Stability Analyzer

Plugin: `com.github.skydoves.compose.stability.analyzer` version `0.7.0`

### Setup

**`app/build.gradle.kts`:**
```kotlin
alias(libs.plugins.stability.analyzer)

composeStabilityAnalyzer {
    stabilityValidation {
        enabled.set(true)
        outputDir.set(layout.projectDirectory.dir("stability"))
        includeTests.set(false)
        ignoreNonRegressiveChanges.set(true)
        failOnStabilityChange.set(System.getenv("CI") == "true")
    }
}
```

`failOnStabilityChange` only fails on CI (`CI=true`) — locally it logs a warning only. `ignoreNonRegressiveChanges` means improvements (UNSTABLE → STABLE) never fail the check.

### Baseline

The baseline file is generated in `app/stability/` and **committed to the repo**. The CI `stabilityCheck` task compares against this baseline — any new UNSTABLE composable that is not in the baseline fails the build.

`app/stability/` is **not** in `.gitignore`.

### Gradle tasks

```bash
./gradlew stabilityDump       # Regenerate baseline for all variants (debug + release)
./gradlew debugStabilityDump  # Regenerate for debug only
./gradlew stabilityCheck      # Compare current composables against baseline — fails on regression
```

Always run `./gradlew stabilityDump` (not `debugStabilityDump`) before committing — otherwise the `releaseStabilityCheck` in CI will fail for the release variant.

### `@IgnoreStabilityReport`

Applied to composables that are intentionally UNSTABLE due to ViewModel parameters:

```kotlin
@IgnoreStabilityReport
@Composable
fun HomeScreen(..., viewModel: HomeViewModel) { ... }

@IgnoreStabilityReport
@Composable
fun DetailsScreen(..., viewModel: DetailsViewModel) { ... }

@IgnoreStabilityReport
@Composable
fun SplashScreen(..., viewModel: SplashViewModel) { ... }
```

ViewModels are always UNSTABLE (they have `StateFlow` and mutable properties). The pattern of extracting state in `Screen` and passing it down to a pure `Content` composable already isolates recompositions correctly — annotating the Screen with `@IgnoreStabilityReport` prevents false positives in the stability check.

### Current stability report

All composables are `skippable: true` except:
- `HomeScreen`, `DetailsScreen`, `SplashScreen` — UNSTABLE due to ViewModel (expected, annotated with `@IgnoreStabilityReport`)
- `SplashContent` — UNSTABLE due to `LottieComposition?` parameter (external library, not actionable)

### IDE Heatmap (Recomposition Heatmap)

The IDE plugin (`View → Tool Windows → Compose Stability Analyzer → Start Recomposition Heatmap`) requires ADB. **It does not work on Windows** — the plugin cannot resolve ADB even with `ANDROID_HOME`, `ANDROID_SDK_ROOT`, and `platform-tools` on PATH correctly configured. This is a known bug of the plugin on Windows.

Use the Android Studio **Layout Inspector** (`View → Tool Windows → Layout Inspector`) as an alternative for runtime recomposition counts.

---

## Static Analysis — Detekt

Config file: `config/detekt.yml` at project root.

### Detekt Compose rules
- Plugin: `io.nlopez.compose.rules:detekt:0.4.27` (latest version compatible with Detekt 1.23.8)
- Version `0.5.x+` requires Detekt 2.x (still in alpha, not yet available in public repos)
- Declared as `compileOnly` in `build-logic/build.gradle.kts` to avoid classpath conflicts
- Added as `detektPlugins` in `CommonSetupPlugin.setupDetekt()` so all modules get it
- Rules configured under the `Compose:` section in `config/detekt.yml`

### Gradle Tasks
| Task | Description |
|---|---|
| `./gradlew detektAll` | Runs Detekt on all modules |
| `./gradlew :module:detekt` | Runs Detekt on a specific module |
| `./gradlew check` | All checks including Detekt, Spotless and tests per module |

## Code Formatting — Spotless

- KtLint via Spotless
- Line endings: `UNIX`
- Config in `setupSpotless()` in build-logic
- Connected to `check` task

---

## Testing

Tests colocated in the module they test:

```
module/src/test/kotlin/        → Unit tests
module/src/androidTest/kotlin/ → Instrumented tests
```

### Current Setup

| Library | Version | Usage |
|---|---|---|
| JUnit 5 (Jupiter) | — | Test runner via `android-junit5` plugin |
| MockK | — | Mocking (`mockk()`, `coEvery`, `coVerify`, `confirmVerified`) |
| Kotest | — | Assertions (`shouldBe`, `shouldBeInstanceOf`) |
| Coroutines Test | — | `runTest`, `StandardTestDispatcher` |
| Turbine | 1.2.0 | `StateFlow` / `Flow` assertions (`awaitItem`) |

Turbine is declared in `libs.versions.toml` and added to `:app` as `testImplementation(libs.turbine)`.

### Dispatcher setup
```kotlin
private val testDispatcher = StandardTestDispatcher()

@BeforeEach fun setUp() { Dispatchers.setMain(testDispatcher) }
@AfterEach fun tearDown() { Dispatchers.resetMain() }

// Advance coroutines in tests that use viewModelScope.launch:
testDispatcher.scheduler.advanceUntilIdle()
// Advance only queued coroutines without running time-based delays:
testDispatcher.scheduler.runCurrent()
```

`StandardTestDispatcher` is used (not `UnconfinedTestDispatcher`) so coroutine execution is explicit and deterministic.

### ViewModel test structure

All ViewModel tests follow these conventions:

**Naming — Given/When/Then**
```kotlin
@Test
fun `Given X, When Y, Then Z`() { ... }
```

**setUp scope**
`@BeforeEach setUp()` initialises only the mocks that the ViewModel constructor requires. Mocks for use-case calls are set up inside each individual test, not in `setUp`.

**Turbine usage**
```kotlin
viewModel.state.test {
  val initial = awaitItem()   // initial state emitted on subscription
  // trigger action
  advanceUntilIdle()
  val next = awaitItem()      // next state after coroutine completes
}
```

`cancelAndIgnoreRemainingEvents()` is used only when:
- Testing only the initial state (no further emissions expected in the assertion)
- Testing tracker-only behaviour where the state does not change

For tests that verify a full state transition, prefer `cancelAndConsumeRemainingEvents()` or explicit `awaitItem()` calls.

### Turbine + advanceUntilIdle ordering

When a coroutine inside the ViewModel emits to a StateFlow, the sequence inside `test { }` must be:

```kotlin
viewModel.state.test {
  awaitItem()                              // consume initial state first
  viewModel.someAction()                   // trigger the coroutine
  advanceUntilIdle()                       // let the coroutine run to completion
  val result = awaitItem()                 // now the new emission is available
  result shouldBe expectedState
}
```

Calling `awaitItem()` before `advanceUntilIdle()` will suspend the test indefinitely because the coroutine hasn't run yet.

### Concurrent guard test pattern

To verify the `isLoadingPage` guard (prevents duplicate in-flight requests), the use case must be kept suspended while the second call arrives. Use `coAnswers` with a virtual `delay`:

```kotlin
@Test
fun `Given loading in progress, When getMovies is called again, Then use case is called only once`() = runTest {
    coEvery { mockedGetMoviesUseCase(validPage) } coAnswers {
      kotlinx.coroutines.delay(1_000)
      domainMovie.right()
    }

    homeViewModel.state.test {
      awaitItem() // estado inicial — StateFlow no re-emite Loading porque ya lo tiene

      homeViewModel.getMovies(validPage)          // lanza coroutine, queda suspendida en delay
      testDispatcher.scheduler.runCurrent()       // ejecuta hasta el suspend point, isLoadingPage = true
      homeViewModel.getMovies(validPage)          // ignorada — isLoadingPage == true
      testDispatcher.scheduler.advanceUntilIdle() // completa el delay

      awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())
    }
  }
```

> **Nota**: `StateFlow` no re-emite `Loading` en page 1 si ya tiene ese valor — no añadir un `awaitItem() shouldBe Loading` extra antes de `MovieListComplete`.

`runCurrent()` advances only the coroutines already queued, without advancing virtual time — this ensures the guard check happens while `isLoadingPage` is still `true`.

### WhileSubscribed + Turbine

`StateFlow` built with `SharingStarted.WhileSubscribed` requires an active collector to start the upstream. The Turbine `.test {}` block acts as that collector automatically — no manual `collect {}` job is needed. Subscribing inside `.test {}` is sufficient:

```kotlin
homeViewModel.layoutMode.test {
  awaitItem() shouldBe LayoutModeUi.Grid2
  cancelAndIgnoreRemainingEvents()
}
```

### Debounce testing pattern

Tests that exercise the search debounce advance the virtual clock past the 500ms debounce window:
```kotlin
viewModel.onSearchQueryChange(query)
testDispatcher.scheduler.advanceTimeBy(501)
testDispatcher.scheduler.advanceUntilIdle()
```

### StateFlow deduplication

`StateFlow` drops emissions that are equal to the current value. When testing paginated data, each page fixture must use **different IDs** to guarantee the new emission is not dropped:
```kotlin
// Page 1: ids 1..20
val domainMovie = DomainMovie(page = 1, results = (1..20).map { ... })
// Page 2: ids 21..40 — NOT 1..20 again
val domainMovie2 = DomainMovie(page = 2, results = (21..40).map { ... })
```

### confirmVerified after getMovies

Any test calling `getMovies()` must verify `trackPageLoaded` in its `confirmVerified` block, because `getMovies` always calls `tracker.trackPageLoaded(page)` on success:
```kotlin
coVerify { tracker.trackPageLoaded(1) }
confirmVerified(getMoviesUseCase, saveMovieUseCase, tracker)
```

### Pure delegator UseCases — 2 tests only

UseCases that do nothing except delegate to a repository (no branching, no transformation) need exactly **2 tests**: success and one error. Testing every error type is redundant because the UseCase has no `when` over the error type — all errors follow the exact same path.

```kotlin
// ✅ Correct — 2 tests for a pure delegator
@Test
fun `Given a valid page, When invoke is called, Then movies are returned`() = runTest { ... }

@Test
fun `Given a repository error, When invoke is called, Then error is returned`() = runTest { ... }
```

UseCases with real logic (e.g. `MustUpdateUseCase`) are tested exhaustively per branch.

### DAOs — instrumentation tests only

`MoviesDao` and all other Room DAOs are excluded from JaCoCo (`**/dao/**`) and have no unit test file. Room needs the Android runtime to operate — even an in-memory database cannot be created in a JVM unit test without Robolectric (removed from the project). If DAO behaviour needs to be verified, use instrumentation tests in `androidTest/` with `Room.inMemoryDatabaseBuilder`.

### NetworkHandler — `when` instead of `?.right() ?:` for JaCoCo

JaCoCo generates 4 branches for a `?.right() ?:` expression because it treats the safe call and the elvis as independent null checks. One of those branches (`right()` returning null) is unreachable in practice, leaving a permanent `pc bpc`. The fix is to use `when` so each branch is explicit and reachable:

```kotlin
// ❌ JaCoCo sees 4 branches, one unreachable
response.body()?.right() ?: UnrecognizedRemoteError().left()

// ✅ JaCoCo sees 2 branches, both reachable
when (val body = response.body()) {
    null -> UnrecognizedRemoteError().left()
    else -> body.right()
}
```

Same pattern applies to `body?.string() ?: ""` in `checkErrorResponse`:
```kotlin
// ❌
json.decodeFromString<ErrorResponse>(body?.string() ?: "")

// ✅
private fun checkErrorResponse(body: ResponseBody?): Option<ErrorResponse> = when (body) {
    null -> None
    else -> Either.catch { json.decodeFromString<ErrorResponse>(body.string()) }.getOrNone()
}
```

### MovieTrackerTest — Bundle limitation

`Bundle` is an Android SDK class — its methods (`getString`, `getLong`) return `null`/`0` in JVM unit tests without Robolectric (removed from the project). `MovieTrackerTest` therefore verifies only the event name, using `any()` for the Bundle parameter. To verify individual Bundle parameters, Robolectric would need to be reintroduced or a custom analytics wrapper created that avoids `Bundle` directly.

### Completed test suites

| Test class | Module | Status |
|---|---|---|
| `HomeViewModelTest` | `:app/presentation` | ✅ Complete |
| `DetailsViewModelTest` | `:app/presentation` | ✅ Complete |
| `SplashViewModelTest` | `:app/presentation` | ✅ Complete |
| `MovieTrackerTest` | `:app/tracking` | ✅ Complete |
| `MoviesDataRepositoryTest` | `:data` | ✅ Complete |
| `RemoteConfigDataRepositoryTest` | `:data` | ✅ Complete |
| `DataStoreDataRepositoryTest` | `:data` | ✅ Complete |
| `MoviesStorageTest` | `:database` | ✅ Complete |
| `GetLayoutModeUseCaseTest` | `:domain` | ✅ Complete |
| `GetMovieDetailsUseCaseTest` | `:domain` | ✅ Complete |
| `GetMoviesUseCaseTest` | `:domain` | ✅ Complete |
| `MustUpdateUseCaseTest` | `:domain` | ✅ Complete |
| `SaveLayoutModeUseCaseTest` | `:domain` | ✅ Complete |
| `SaveMovieUseCaseTest` | `:domain` | ✅ Complete |
| `SearchMoviesUseCaseTest` | `:domain` | ✅ Complete |
| `RemoteConfigServiceTest` | `:remote` | ✅ Complete |
| `MoviesServiceTest` | `:remote` | ✅ Complete |
| `NetworkHandlerTest` | `:remote` | ✅ Complete |

### Test fixtures (package `jsanzo.movies.ui.model`)

| Fixture | Description |
|---|---|
| `movieUi` | `MovieUi` with `backdropPath = null` |
| `domainMovieResult` | `DomainMovieResult` with `backdropPath = null`, `id = 1` |
| `domainMovie` | `DomainMovie` page 1, results ids 1..20 |
| `domainMovie2` | `DomainMovie` page 2, results ids 21..40 (different ids to avoid StateFlow dedup) |
| `domainMovieDetails` | `DomainMovieDetails` fixture for `DetailsViewModelTest` |

### Key decisions recorded during ViewModel migration

- **`onStart { getMovies() }` removed from `HomeViewModel`**: the initial load is now triggered by `LaunchedEffect` in `HomeScreen.kt`. This makes the ViewModel easier to test (no auto-trigger on construction) and avoids double-loading when the screen re-enters composition.
- **`DetailsViewModel.getDetails` does not emit `Loading` before the use-case call**: the ViewModel is always recreated on navigation, so it already starts in the `Loading` state. Emitting it again would cause a redundant, testable-but-meaningless emission.
- **`saveMovie` requires `advanceUntilIdle()`**: it launches a coroutine internally; without advancing the scheduler the use-case mock is never called.
- **`notifyLastElementVisible` requires `advanceUntilIdle()`**: it delegates to `checkNeedNewPage` which may call `getMovies`, which is a coroutine.
- **ViewModels moved to `presentation/`**: separated from UI screens to make the package structure reflect the MVVM separation more clearly. ViewState sealed classes remain in `ui/screens/` alongside their screens.

### Run Tests
```bash
./gradlew testAll                          # All modules
./gradlew :app:testDebugUnitTest           # Specific module
```

### Code Coverage — JaCoCo

JaCoCo 0.8.12 is configured via `setupJacocoReport()` in `CommonSetupPlugin`. Each module gets a `jacocoDebugTestReport` task that generates both HTML and XML reports. A merged report aggregating all modules is generated via `jacocoMergedReport` at the root level.
- **Minimum Threshold**: 95% (verified by `jacocoMergedCoverageVerification`).
- **Reports**: HTML/XML reports generated at root level. Coverage data is reported to Codecov.
- **Exclusions**: Compose internals, generated code, DI, and UI-only packages (theme, navigation, etc.) are excluded from reports.

```bash
./gradlew jacocoAll                        # Run JaCoCo on all modules (individual reports)
./gradlew jacocoMergedReport               # Generate merged report for all modules
./gradlew jacocoMergedCoverageVerification # Run tests, report and verify 95% threshold
./gradlew :app:jacocoDebugTestReport       # Specific module
```

Individual reports are generated at `<module>/build/reports/jacoco/jacocoDebugTestReport/`.
Merged report is generated at `build/reports/jacoco/jacocoMergedReport/html/index.html`.

The excludes list is defined once in `setupJacocoReport()` and exposed via `project.extra["jacocoExcludes"]` so the root `build.gradle.kts` can reuse it without duplication.

**Excluded from coverage:**
- Generated code (`**/generated/**`, `**/ksp/**`)
- DI modules (`**/di/**`, `**/*Module*.*`, `**/*Component*.*`)
- Android boilerplate (`**/R.class`, `**/BuildConfig.*`, `**/Manifest*.*`)
- Koin generated classes (`**/*_Factory*.*`)
- DAOs (`**/dao/**`) — requires instrumentation tests, not unit tests
- Room TypeConverters (`**/Converters*.*`) — infrastructure boilerplate
- Android DataStore (`**/DataStore*.*`) — requires instrumentation tests, not unit tests
- Kotlin internal classes (lambdas, anonymous classes, `WhenMappings`, `DefaultImpls`)
- UI boilerplate (`**/ui/theme/**`, `**/ui/navigation/**`, `**/ui/screens/**/*Screen*`, etc.)

### Codecov

Coverage reports are uploaded to [Codecov](https://app.codecov.io/github/jsanzo97/movies) on every PR and on every push to `develop` via `codecov/codecov-action@v4`.

- Coverage badge is dynamic and updates automatically with each merge to `develop`
- The merged report XML (`build/reports/jacoco/jacocoMergedReport/jacocoMergedReport.xml`) is uploaded, covering all modules
- `CODECOV_TOKEN` stored as GitHub Actions Secret
- Codecov PR comments disabled via `comment: false`

**Important:** `isReturnDefaultValues = true` is set in `app/build.gradle.kts` `testOptions` to allow Android SDK classes (like `Bundle`) to return default values instead of throwing in unit tests. This is required for `MovieTrackerTest`.

---

## CI/CD & Branching

### Branching Strategy (Git Flow)

| Branch | Purpose |
|---|---|
| `main` | Production-ready code |
| `develop` | Integration branch |
| `feature/` | New features |
| `refactor/` | Refactoring work |
| `bugfix/` | Bug fixes |
| `hotfix/` | Critical production fixes |

### GitHub Actions — PR Validation

File: `.github/workflows/pr-validation.yml`

- Triggers on every PR (`on: pull_request`)
- Cancels in-progress runs when new commit is pushed (`cancel-in-progress: true`)
- `JAVA_TOOL_OPTIONS: "-Djava.awt.headless=true"` set on all Gradle steps to suppress KSP NullPointerException in headless CI environments
- Gradle cache managed by `gradle/actions/setup-gradle@v4` with `cache-read-only: false` to allow cache writes on every run
- `cache: 'gradle'` on `actions/setup-java@v4` restores Gradle User Home before `setup-gradle` runs
- `google-services.json` decoded from secret before every Gradle task that needs it
- Jobs:
    1. `check` — Detekt + Spotless (runs first)
    2. `stability-check` — `./gradlew stabilityCheck` comparing against committed baseline (runs after check, in parallel with build-and-test)
    3. `build-and-test` — `assembleDebug` + `jacocoMergedCoverageVerification` (Min 95% code coverage) + Codecov upload (runs after check, in parallel with stability-check)
- **Quality Gate**: The build fails automatically if the aggregated coverage is below **95%**.
- **Reports**: Coverage reported to **Codecov**
- Codecov PR comments disabled via `comment: false`

**Approximate CI times after cache optimization:**
- `check`: ~1 min
- `stability-check`: ~1 min (runs in parallel with build-and-test)
- `build-and-test`: ~2 min

### GitHub Actions — Coverage

File: `.github/workflows/coverage.yml`

- Triggers on push to `develop` only (`on: push: branches: [develop]`)
- Runs after a PR is merged — avoids re-running the full validation pipeline on develop
- Single job: `jacocoMergedReport` + Codecov upload
- Much faster than full PR validation since build and tests already passed in the PR

> **Why a separate workflow?** PRs already run the full pipeline. Running it again on develop after merge is redundant. The coverage workflow only does what's needed to update Codecov and the badge.

**GitHub Actions Secrets required:**
- `SERVER_ENDPOINT`
- `SERVER_API_KEY`
- `GOOGLE_SERVICES_JSON` (base64 encoded `google-services.json`, generated with PowerShell: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("D:\path\to\app\google-services.json")) | clip`)
- `CODECOV_TOKEN` (from codecov.io dashboard)

> **Branch protection rules**: if status checks are required on `develop`/`main`, the required check names are `Check (Detekt + Spotless)` and `Build & Tests`.

### Gradle performance settings (`gradle.properties`)

```properties
org.gradle.jvmargs=-Xmx6144m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.configuration-cache=true
org.gradle.caching=true
org.gradle.daemon=false
```

- `-Xmx6144m` — capped at 6GB to stay within GitHub runner memory limits (7GB available)
- `org.gradle.daemon=false` — daemon brings no benefit on ephemeral CI runners
- `org.gradle.configuration-cache=true` — enabled globally, applies to both local and CI
- `org.gradle.caching=true` — build cache enabled globally

### Fastlane (CD)

El proyecto utiliza Fastlane para automatizar los despliegues y la gestión de metadatos.

| Lane | Descripción |
|---|---|
| `deploy_internal` | Compila el bundle de release y lo sube al track de Pruebas Internas de Google Play. |
| `download_metadata` | Sincroniza los textos e imágenes desde Google Play Console al proyecto local. |
| `build_release` | Genera el App Bundle (.aab) firmado localmente. |

**Archivos clave:**
- `fastlane/Appfile`: Configuración del package name y ruta de la llave JSON.
- `fastlane/Fastfile`: Definición de las tareas de automatización.
- `fastlane/metadata/`: Almacena los textos de la tienda (títulos, descripciones, changelogs) en `es-ES` y `en-GB`.
- `Gemfile`: Gestiona la versión de Fastlane y sus dependencias en Ruby.

---

## Git Hooks

File: `config/git-hooks/commit-msg`

Install: `./gradlew installGitHooks`

### Commit format
```
[Task - 123] your commit message here
```

### Hook behaviour
- If the commit message already contains `[Task - N]` it validates and exits
- If not, it finds the highest task number across **all branches** (`git log --all`) and auto-prepends `[Task - N+1]`
- Skips automatic git commits (merge, rebase, fixup, squash)
- Rejects commits on branches that don't match `feature/`, `refactor/`, `bugfix/`, `hotfix/`
- Rejects commit messages shorter than 10 characters

---

## API Configuration

```
Base URL:  https://api.themoviedb.org/3/ or BuildConfig.SERVER_ENDPOINT
API Key:   BuildConfig.SERVER_API_KEY
Image URL: https://image.tmdb.org/t/p/original (defined as BASE_IMAGE_URL_ORIGINAL in screen files)
Search:    GET search/movie?query=...
```

### Secrets management

API keys and URLs are never hardcoded in source code. They are read from `local.properties` locally and from GitHub Actions Secrets in CI.

**`local.properties`** (gitignored, local only):
```properties
SERVER_ENDPOINT=https://api.themoviedb.org/3/
SERVER_API_KEY=your_api_key_here
```

**`:remote/build.gradle.kts`** reads from `local.properties` with fallback to environment variables for CI:
```kotlin
val localProperties = Properties().apply {
  val file = rootProject.file("local.properties")
  if (file.exists()) load(file.inputStream())
}

buildConfigField(
  "String", "SERVER_ENDPOINT",
  "\"${localProperties["SERVER_ENDPOINT"] ?: System.getenv("SERVER_ENDPOINT")}\"",
)
buildConfigField(
  "String", "SERVER_API_KEY",
  "\"${localProperties["SERVER_API_KEY"] ?: System.getenv("SERVER_API_KEY")}\"",
)
```

**GitHub Actions Secrets** required (Settings → Secrets and variables → Actions):
- `SERVER_ENDPOINT`
- `SERVER_API_KEY`
- `GOOGLE_SERVICES_JSON` (base64 encoded `google-services.json`)

Secrets are injected as environment variables in the `build-and-test` job only (the `check` job does not compile code so does not need them).

---

---

## Obfuscation & ProGuard

- **Modularization**: Each module defines its own `proguard-rules.pro` and exports them via `consumerProguardFiles` in `SetupAndroidLibraryPlugin`.
- **Minification**: Only enabled in the `:app` module (`isMinifyEnabled = true` for `release` build type). Libraries do not minify themselves to avoid "missing classes" errors during consolidation of generated code (Koin KSP, Room).
- **Key Rules**:
    - `:remote`: Protects Retrofit interfaces and Kotlinx Serialization DTOs.
    - `:database`: Protects Room entities and DAOs.
    - `:domain`: Protects Arrow types.
    - `:app`: Protects Koin injection, Compose runtime, Lottie, and Firebase.

---

## Release Management

### Release Signing
The app is signed using a Keystore managed locally via `keystore.properties` (ignored from Git). A `keystore.properties.example` is provided as a template.

### Automated Versioning
CI/CD automates version bumping in `gradle/libs.versions.toml`. It supports `major`, `minor`, and `patch` increments.

### Google Play Deployment (Fastlane)
Manual GitHub Actions workflow (`google-play-deploy.yml`) that triggers Fastlane's `deploy_play_store` lane. It allows choosing the target track (`internal`, `beta`, `production`). It performs full project verification (`check` task) and builds the AAB before uploading. Metadata, image, and screenshot uploads are skipped to allow manual management of "What's New" and store assets directly in the Google Play Console.

---

## CI/CD (Firebase Deployment)

A manual GitHub Actions workflow allows deploying `debug` or `release` builds to Firebase App Distribution.

**Features:**
- **Manual Trigger**: Uses `workflow_dispatch` with inputs for `variant` and `version_type`.
- **Automatic Bumping**: Increments version and commits changes back to the repository using a Personal Access Token (`GH_PAT`) to bypass branch protection.
- **Release Notes**: Automatically generates release notes by collecting commit messages since the last `chore: bump version` commit.
- **Security**: Handles Keystore, `google-services.json`, and Firebase Service Account JSON via GitHub Secrets.
- **Robustness**: Version bump is committed only after a successful deployment.

## Version Catalog

| Library | Version |
|---|---|
| Android Gradle Plugin | 9.0.1 |
| Kotlin | 2.3.10 |
| Coroutines | 1.10.2 |
| Koin | 4.1.1 |
| Koin Annotations | 2.3.1 |
| Retrofit | 3.0.0 |
| OkHttp | 5.3.2 |
| Room | 2.8.4 |
| Navigation3 | 1.0.1 |
| Compose BOM | 2026.03.00 |
| Coil | 2.7.0 |
| Accompanist | 0.37.3 |
| Arrow | 2.2.1.1 |
| Detekt | 1.23.8 |
| detekt-rules-compose | 0.4.27 |
| Firebase BOM | 34.9.0 |
| Google Services plugin | 4.4.4 |
| Firebase Crashlytics plugin | 3.0.6 |
| Lifecycle | 2.10.0 |
| KSP | 2.3.5 |
| Lottie | 6.6.6 |
| Turbine | 1.2.0 |
| compose-stability-analyzer | 0.7.0 |

---

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Tests
./gradlew testAll
./gradlew :app:testDebugUnitTest

# Coverage
./gradlew jacocoAll                        # individual reports per module
./gradlew jacocoMergedReport               # merged report for all modules
./gradlew :app:jacocoDebugTestReport       # specific module
./gradlew jacocoMergedCoverageVerification # coverage verification

# Static analysis
./gradlew detektAll
./gradlew :app:detekt

# All checks
./gradlew check

# Stop Gradle daemon (Windows file lock workaround)
./gradlew --stop

# Install git hooks
./gradlew installGitHooks

# Compose Stability
./gradlew stabilityDump       # Regenerate baseline for all variants
./gradlew stabilityCheck      # Check composables against baseline (fails on regression)
```

---

## Known Issues & Notes

- **Windows file locking**: The Gradle daemon may lock `.jar` files. Run `./gradlew --stop` if the build fails with file access errors.
- **Namespace special case**: The `:app` module has a hardcoded `if` in `calculateNamespace()` because its namespace is `jsanzo.movies`, not `jsanzo.movies.app`.
- **Compose compiler plugin classpath conflict**: With AGP 9.0.1 + Kotlin 2.x, applying `org.jetbrains.kotlin.plugin.compose` via `pluginManager.apply()` from a convention plugin in an included build causes `org.jetbrains:annotations` version conflicts. Workaround: declare it with `apply false` in the root `build.gradle.kts` and apply it explicitly in `app/build.gradle.kts`.
- **detekt-rules-compose version cap**: Versions `0.5.x+` depend on `dev.detekt 2.0.0-alpha.2` which is not yet published in public repos. Max compatible version with Detekt 1.23.8 is `0.4.27`.
- **KSP NullPointerException in CI**: KSP throws a harmless `NullPointerException` in `AWT-EventQueue-0` on headless environments. Does not fail the build. Suppressed via `JAVA_TOOL_OPTIONS: "-Djava.awt.headless=true"` in CI.
- **Bundle not readable in JVM unit tests**: `Bundle` methods return `null`/`0` without Robolectric. `MovieTrackerTest` verifies only event names as a result — see testing section for details.
- **Compose Stability Analyzer Heatmap on Windows**: The `Start Recomposition Heatmap` button in the IDE plugin tool window fails with `Cannot find adb` on Windows even with `ANDROID_HOME`, `ANDROID_SDK_ROOT`, and `platform-tools` on PATH. This is a known plugin bug. Use Layout Inspector as an alternative for runtime recomposition counts.

---

## Pending Migrations

- [x] Migrate Home screen to Jetpack Compose
- [x] Migrate Details screen to Jetpack Compose
- [x] Migrate Navigation to Navigation Compose
- [x] Migrate Navigation Compose to Navigation3
- [x] Remove XML layouts, Fragments, Safe Args and related dependencies
- [x] Restructure project defining Koin modules on its module instead of app
- [x] Add Compose + Navigation Compose setup
- [x] Add detekt-rules-compose with Compose rules in detekt.yml
- [x] Apply kotlinx-serialization plugin via CommonSetupPlugin to all modules
- [x] Set up GitHub Actions CI/CD pipelines (PR validation)
- [x] Migrate tests to JUnit 5 with `android-junit5` (Mannodermaus)
- [x] Remove Robolectric dependency
- [x] Migrate theme to Material 3 with M3 color tokens, typography scale and dynamic color
- [x] Add animated Lottie splash screen
- [x] Add Firebase Remote Config force update check
- [x] Implement Force Update screen (Lottie animation, themed colors, Play Store deep link)
- [x] Add accessibility semantics (paneTitle, heading, mergeDescendants, contentDescription, Role) across all screens
- [x] Add RTL support in navigation animations
- [x] Implement search against TMDB API (`/search/movie` endpoint) with debounce (500ms) and local DB fallback
- [x] Add empty search state (icon + text) with accessibility semantics
- [x] Add `trackSearchPerformed` event to `MovieTracker` and `FirebaseTracker`
- [x] Add `@Immutable`/`@Stable` annotations to `HomeViewState`, `DetailsViewState`, `MovieUi`, `MovieDetailsUi`
- [x] Migrate `MoviesSuccess.movies` from `List<DomainMovieResult>` to `ImmutableList<MovieUi>` (kotlinx-collections-immutable)
- [x] Introduce `:datastore` module with `DataStoreStorageImpl`, `DataStoreRepositoryImpl`, `GetLayoutModeUseCase`, `SaveLayoutModeUseCase`
- [x] Persist layout mode preference via DataStore, expose as `StateFlow<LayoutModeUi>` in `HomeViewModel`
- [x] Add `trackLayoutModeChanged` event to `MovieTracker` and `FirebaseTracker`
- [x] Move ViewModels from `ui/screens/` to `presentation/` package
- [x] Add StrictMode in debug builds (`MoviesApplication`)
- [x] Add Compose Stability Analyzer plugin (Gradle + IDE), baseline committed in `app/stability/`, `stabilityCheck` enforced in CI
- [x] Remove translucent theme properties to fix screen rotation issue
