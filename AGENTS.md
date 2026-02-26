# agents.md — Movies Android Project

> **Maintenance**: This file should be updated automatically whenever relevant changes occur in the project — new libraries, migrations, plugins, architectural decisions, or structural changes. Also update it when explicitly requested.

---

## Project Overview

Multi-module Android application built with Kotlin that displays movies using The Movie Database (TMDB) API. Follows Clean Architecture with MVVM presentation pattern. Fully migrated to Jetpack Compose and Navigation Compose. Migrating from JUnit 4 to JUnit 5.

---

## Tech Stack

| Area | Technology | Notes |
|---|---|---|
| Language | Kotlin 2.3.10 | |
| UI | Jetpack Compose | Migration from XML/Fragments complete |
| Architecture | MVVM + Clean Architecture | |
| DI | Koin 4.1.1 | Using Koin Annotations for DI |
| Navigation | Navigation Compose | Migration from Jetpack Navigation Component + Safe Args complete |
| Networking | Retrofit 3.0.0 + OkHttp 5.3.2 | |
| Serialization | Kotlinx Serialization 1.10.0 | Applied via CommonSetupPlugin to all modules |
| Database | Room 2.8.4 | |
| Async | Coroutines 1.10.2 + Flow + StateFlow | |
| Image loading | Coil 2.7.0 | Replaced Glide |
| Error handling | Arrow 2.2.1.1 (Either, Option) | Under evaluation, may be removed |
| Analytics & Crashlytics | Firebase BOM 33.7.0 | Crashlytics + Analytics with DebugView |
| HTTP inspector | Chucker | debugImplementation only, no-op in release |
| Static analysis | Detekt 1.23.8 | |
| Build system | Gradle 9.3.1 (Kotlin DSL) | |
| Min SDK | 24 | |
| Target/Compile SDK | 36 | |
| Java compatibility | Java 21 | |

---

## Module Structure

```
Movies/
├── app/             # Application module (com.android.application)
├── domain/          # UseCases, Entities, Repository interfaces, Errors
├── data/            # Repository implementations, Data entities
├── database/        # Room database, DAOs, DB entities
├── remote/          # Retrofit API, Remote DTOs, Remote data sources
└── build-logic/     # Convention plugins (Gradle build logic)
```

### Dependency Graph

```
app      → domain, data, database, remote
data     → domain, database, remote
remote   → domain (for interfaces)
database → domain (for interfaces)
domain   → (no dependencies)
```

---

## Package Naming Convention

Base package: `jsanzo.movies`

Each module appends its name automatically via `calculateNamespace()` in build-logic:
- `:domain` → `jsanzo.movies.domain`
- `:data` → `jsanzo.movies.data`
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

### Presentation Layer (`:app`)
- ViewModels expose `StateFlow<ViewState>`.
- Sealed classes for ViewState per screen.
- Compose screens in `ui/screens/`.

---

## Compose Setup

Compose is configured in `SetupAndroidApplicationPlugin` (`:app` only). Key decisions:

### Plugin setup
- `buildFeatures.compose = true` is set inside `SetupAndroidApplicationPlugin`
- The `org.jetbrains.kotlin.plugin.compose` plugin is declared with `apply false` in the **root** `build.gradle.kts` to make it available on the classpath without triggering classpath conflicts
- It is then applied explicitly in `app/build.gradle.kts` via `alias(libs.plugins.compose.compiler)`

> **Why not apply it from the convention plugin?** Applying it programmatically via `pluginManager.apply()` from `build-logic` causes `org.jetbrains:annotations` version conflicts with AGP 9.0.1 and Kotlin 2.x embedded in Gradle. The `apply false` in root + explicit apply in `:app` is the correct workaround.

### Compose dependencies (added in `SetupAndroidApplicationPlugin`)
```kotlin
"implementation"(platform(libs().getLibrary("compose-bom")))
"implementation"(libs().getLibrary("compose-ui"))
"implementation"(libs().getLibrary("compose-material3"))
"implementation"(libs().getLibrary("compose-ui-tooling-preview"))
"implementation"(libs().getLibrary("navigation-compose"))
"implementation"(libs().getLibrary("androidx-lifecycle-runtime-compose"))
"implementation"(libs().getLibrary("koin-compose"))
"implementation"(libs().getLibrary("coil-compose"))
"implementation"(libs().getLibrary("material-icons-core"))
"implementation"(libs().getLibrary("accompanist-systemuicontroller"))
"implementation"(libs().getLibrary("accompanist-permissions"))
"debugImplementation"(libs().getLibrary("compose-ui-tooling"))
```

### Compose screen structure in `:app`
```
app/src/main/kotlin/jsanzo/movies/
├── di/
│   └── AppModule.kt
├── ui/
│   ├── navigation/
│   │   ├── AppDestinations.kt
│   │   └── AppNavigation.kt
│   └── screens/
│       ├── home/
│       │   ├── HomeScreen.kt
│       │   ├── HomeViewModel.kt
│       │   └── HomeViewState.kt
│       └── details/
│           ├── DetailsScreen.kt
│           ├── DetailsViewModel.kt
│           └── DetailsViewState.kt
├── theme/
│   └── MoviesTheme.kt
├── ComposeActivity.kt
├── Constants.kt
└── MoviesApplication.kt
```

### Navigation
Type-safe Navigation Compose using `@Serializable` data objects/classes:
```kotlin
sealed interface AppDestinations {
  @Serializable data object Home : AppDestinations
  @Serializable data class Details(val movieId: Int) : AppDestinations
}
```

`ComposeActivity` is the sole launcher Activity. All XML navigation, Fragments, Safe Args, and related dependencies have been removed.

---

## Theme

`MoviesTheme` in `ui/theme/MoviesTheme.kt` defines light and dark color schemes based on the existing XML theme colors. Respects system dark mode via `isSystemInDarkTheme()`. Status bar color is set via `accompanist-systemuicontroller`.

Colors:
- Primary: `#FF6200EE` (Purple500)
- PrimaryContainer: `#FF3700B3` (Purple700)
- Secondary: `#FF03DAC5` (Teal200)
- SecondaryContainer: `#FF018786` (Teal700)

`ComposeActivity` uses `@style/AppTheme.NoActionBar` in the manifest to avoid a black background flash before Compose renders.

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

### Koin Module Structure

| Koin Module | Location | Purpose |
|---|---|---|
| `AppModule` | `:app/di` | Main module, includes all other modules. |
| `DataModule` | `:data/di` | Provides repository implementations. |
| `DatabaseModule`| `:database/di`| Provides Room DB, DAOs, and the local datastore. |
| `RemoteModule` | `:remote/di` | Provides the remote datastore (`MoviesService`). |
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

### Tracker interface
`Tracker` is an interface defined in `:app/tracking/Tracker.kt`. `FirebaseTracker` implements it. ViewModels depend on `Tracker` not `FirebaseTracker`, making them testable without Firebase:

```kotlin
interface Tracker {
  fun trackHomeShown()
  fun trackDetailsShown(movieId: Int)
  fun trackMovieClicked(movieId: Int, movieTitle: String)
  fun trackErrorShown(screen: String, error: String)
  fun trackPageLoaded(page: Int)
}
```

In tests, `Tracker` is mocked with `mockk(relaxed = true)` so all tracking calls are ignored automatically.

### FirebaseTracker
Located in `:app/tracking/FirebaseTracker.kt`. Single source of truth for all analytics events. Provided via `AppModule`:

```kotlin
@Single
fun provideFirebaseTracker(androidContext: Application): FirebaseTracker =
  FirebaseTracker(
    analytics = FirebaseAnalytics.getInstance(androidContext),
    crashlytics = FirebaseCrashlytics.getInstance(),
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

### Where events are triggered
- `trackHomeShown()` — `HomeViewModel.trackScreenView()` called from `HomeScreen` `LaunchedEffect`
- `trackDetailsShown()` — `DetailsViewModel.trackScreenView(movieId)` called from `DetailsScreen` `LaunchedEffect`
- `trackMovieClicked()` — `HomeViewModel.saveMovie()`
- `trackErrorShown()` — `HomeViewModel.getMovies()` and `DetailsViewModel.getDetails()` on error
- `trackPageLoaded()` — `HomeViewModel.getMovies()` on success

### Pending Firebase functions (not yet implemented)
- `setUserId(userId)` — for when user login is added
- `logError(throwable, message)` — for recording handled errors in Crashlytics without crashing

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

## Home Screen

### ViewState
```kotlin
@Stable
sealed class HomeViewState
data object Loading : HomeViewState()

@Immutable
data class MoviesSuccess(val movies: List<DomainMovieResult>) : HomeViewState()

@Immutable
data class MoviesError(val message: String) : HomeViewState()
```

Navigation to details is handled as a side effect via `SharedFlow<Int>` instead of a ViewState.

### ViewModel key decisions
- `loadingJob` pattern to prevent duplicate page requests during fast scroll:
```kotlin
private var loadingJob: Job? = null

fun getMovies(page: Int = nextPageToRetrieve) {
  if (loadingJob?.isActive == true) return
  loadingJob = viewModelScope.launch { ... }
}
```
- Deduplication using `Set` of IDs: `moviesRetrieved.map { it.id }.toSet()`
- Pagination based on `moviesRetrieved.size` (not a separate counter) to account for filtered duplicates
- `nextPageToRetrieve` incremented only on `onSuccess`
- Loading state only emitted on page 1 to avoid hiding the list during pagination

### Pagination logic
```kotlin
private fun checkNeedNewPage() {
  val totalLoaded = moviesRetrieved.size
  if (lastVisible + threshold >= totalLoaded) {
    getMovies()
  }
}
```

### UI key decisions
- `SearchBar` with `RectangleShape` and `expanded = false` (never expands, no suggestions)
- `SubcomposeAsyncImage` with loading indicator and error fallback (`ic_error_load`)
- `@Stable` / `@Immutable` annotations on ViewState for Compose stability
- `key = { _, movie -> movie.id }` in `itemsIndexed` to prevent duplicate key crashes
- `remember(state, searchQuery)` for filtered movie list to avoid recalculation on every recomposition
- `windowInsetsPadding(WindowInsets.safeDrawing)` on root Column
- `Column` as root container with `SearchBar` fixed at top and `LazyColumn` below
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

| Library | Usage |
|---|---|
| JUnit 5 (Jupiter) | Test runner via `android-junit5` plugin |
| MockK | Mocking (`mockk()`, `coEvery`, `coVerify`) |
| Kotest | Assertions (`shouldBe`, `shouldBeInstanceOf`) |
| Coroutines Test | `runTest`, `UnconfinedTestDispatcher` |

### Dispatcher setup
```kotlin
private val testDispatcher = StandardTestDispatcher()

@BeforeEach fun setUp() { Dispatchers.setMain(testDispatcher) }
@AfterEach fun tearDown() { Dispatchers.resetMain() }

// In tests that use viewModelScope.launch:
testDispatcher.scheduler.advanceUntilIdle()
```

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
- DAOs (`**/dao/**`)
- Kotlin internal classes (lambdas, anonymous classes, `WhenMappings`, `DefaultImpls`)
- UI boilerplate (`**/ui/theme/**`, `**/ui/navigation/**`, `**/ui/screens/**/*Screen*`, etc.)

### Codecov

Coverage reports are uploaded to [Codecov](https://app.codecov.io/github/jsanzo97/movies) on every PR and on every push to `develop` via `codecov/codecov-action@v4`.

- Coverage badge is dynamic and updates automatically with each merge to `develop`
- The merged report XML (`build/reports/jacoco/jacocoMergedReport/jacocoMergedReport.xml`) is uploaded, covering all modules
- `CODECOV_TOKEN` stored as GitHub Actions Secret

**Important:** `isReturnDefaultValues = true` is set in `app/build.gradle.kts` `testOptions` to allow Android SDK classes (like `Bundle`) to return default values instead of throwing in unit tests. This is required for `FirebaseTrackerTest`.

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
  2. `build-and-test` — `assembleDebug` + `jacocoMergedCoverageVerification` (Min 95% code coverage) + Codecov upload (runs after check)
- **Quality Gate**: The build fails automatically if the aggregated coverage is below **95%**.
- **Reports**: Coverage reported to **Codecov**

**Approximate CI times after cache optimization:**
- `check`: ~1 min
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
Base URL:  https://api.themoviedb.org/3/movie/ or BuildConfig.SERVER_ENDPOINT
API Key:   BuildConfig.SERVER_API_KEY
Image URL: https://image.tmdb.org/t/p/original (defined as BASE_IMAGE_URL_ORIGINAL in screen files)
```

### Secrets management

API keys and URLs are never hardcoded in source code. They are read from `local.properties` locally and from GitHub Actions Secrets in CI.

**`local.properties`** (gitignored, local only):
```properties
SERVER_ENDPOINT=https://api.themoviedb.org/3/movie/
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
| Navigation Compose | 2.9.0 |
| Compose BOM | 2025.06.00 |
| Coil | 2.7.0 |
| Accompanist | 0.36.0 |
| Arrow | 2.2.1.1 |
| Detekt | 1.23.8 |
| detekt-rules-compose | 0.4.27 |
| Firebase BOM | 33.7.0 |
| Google Services plugin | 4.4.2 |
| Firebase Crashlytics plugin | 3.0.3 |
| Lifecycle | 2.10.0 |
| KSP | 2.3.5 |

---

## Common Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Testing & Coverage
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
```

---

## Known Issues & Notes

- **Windows file locking**: The Gradle daemon may lock `.jar` files. Run `./gradlew --stop` if the build fails with file access errors.
- **Namespace special case**: The `:app` module has a hardcoded `if` in `calculateNamespace()` because its namespace is `jsanzo.movies`, not `jsanzo.movies.app`.
- **Compose compiler plugin classpath conflict**: With AGP 9.0.1 + Kotlin 2.x, applying `org.jetbrains.kotlin.plugin.compose` via `pluginManager.apply()` from a convention plugin in an included build causes `org.jetbrains:annotations` version conflicts. Workaround: declare it with `apply false` in the root `build.gradle.kts` and apply it explicitly in `app/build.gradle.kts`.
- **detekt-rules-compose version cap**: Versions `0.5.x+` depend on `dev.detekt 2.0.0-alpha.2` which is not yet published in public repos. Max compatible version with Detekt 1.23.8 is `0.4.27`.
- **KSP NullPointerException in CI**: KSP throws a harmless `NullPointerException` in `AWT-EventQueue-0` on headless environments. Does not fail the build. Suppressed via `JAVA_TOOL_OPTIONS: "-Djava.awt.headless=true"` in CI.

---

## Pending Migrations

- [x] Migrate Home screen to Jetpack Compose
- [x] Migrate Details screen to Jetpack Compose
- [x] Migrate Navigation to Navigation Compose
- [x] Remove XML layouts, Fragments, Safe Args and related dependencies
- [x] Restructure project defining Koin modules on its module instead of app
- [x] Add Compose + Navigation Compose setup
- [x] Add detekt-rules-compose with Compose rules in detekt.yml
- [x] Apply kotlinx-serialization plugin via CommonSetupPlugin to all modules
- [x] Set up GitHub Actions CI/CD pipelines (PR validation)
- [ ] Implement search against TMDB API (`/search/movie` endpoint) with debounce (300ms) instead of local filtering — current local search only finds movies already loaded in memory
- [ ] Migrate tests to JUnit 5 with `android-junit5` (Mannodermaus)
- [ ] Remove Robolectric dependency
- [ ] ~~Add Detekt JUnit 5 rules plugin~~ — evaluated and discarded. The available plugin (`de.joshuagleitze:detekt-junit5`) only offers value for preventing JUnit 4/5 mixing, which is already fully migrated. Not worth the dependency.
- [ ] Set up deploy pipeline
- [ ] Evaluate Arrow dependency (keep or remove)
- [ ] Introduce dedicated mapper classes (currently using extension functions)
