# agents.md — Movies Android Project

> **Maintenance**: This file should be updated automatically whenever relevant changes occur in the project — new libraries, migrations, plugins, architectural decisions, or structural changes. Also update it when explicitly requested.

---

## Project Overview

Multi-module Android application built with Kotlin that displays movies using The Movie Database (TMDB) API. Follows Clean Architecture with MVVM presentation pattern. Currently being modernized: migrating from XML/Fragments to Jetpack Compose, and from JUnit 4 to JUnit 5.

---

## Tech Stack

| Area | Technology | Notes |
|---|---|---|
| Language | Kotlin 2.3.10 | |
| UI | Jetpack Compose | Migration from XML/Fragments complete for Home screen |
| Architecture | MVVM + Clean Architecture | |
| DI | Koin 4.1.1 | Using Koin Annotations for DI |
| Navigation | Navigation Compose | Migrated from Jetpack Navigation Component + Safe Args |
| Networking | Retrofit 3.0.0 + OkHttp 5.3.2 | |
| Serialization | Kotlinx Serialization 1.10.0 | Applied via CommonSetupPlugin to all modules |
| Database | Room 2.8.4 | |
| Async | Coroutines 1.10.2 + Flow + StateFlow | |
| Image loading | Coil 2.7.0 | Replaces Glide for Compose screens |
| Error handling | Arrow 2.2.1.1 (Either, Option) | Under evaluation, may be removed |
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
- Compose screens in `ui/compose/`.

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
└── ui/
    └── compose/
        ├── ComposeActivity.kt
        ├── theme/
        │   └── MoviesTheme.kt
        ├── navigation/
        │   ├── AppDestinations.kt
        │   └── AppNavigation.kt
        └── screens/
            ├── home/
            │   ├── HomeScreen.kt
            │   ├── HomeViewModel.kt
            │   └── HomeViewState.kt
            └── details/
                └── DetailsScreen.kt
```

### Navigation
Type-safe Navigation Compose using `@Serializable` data objects/classes:
```kotlin
sealed interface AppDestinations {
    @Serializable data object Home : AppDestinations
    @Serializable data class Details(val movieId: Int) : AppDestinations
}
```

`ComposeActivity` is the launcher Activity. `MainActivity` remains registered in the manifest but is no longer the launcher.

---

## Theme

`MoviesTheme` in `ui/compose/theme/MoviesTheme.kt` defines light and dark color schemes based on the existing XML theme colors. Respects system dark mode via `isSystemInDarkTheme()`. Status bar color is set via `accompanist-systemuicontroller`.

Colors:
- Primary: `#FF6200EE` (Purple500)
- PrimaryContainer: `#FF3700B3` (Purple700)
- Secondary: `#FF03DAC5` (Teal200)
- SecondaryContainer: `#FF018786` (Teal700)

`ComposeActivity` uses `@style/AppTheme.NoActionBar` in the manifest to avoid a black background flash before Compose renders.

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

## Home Screen — Compose Implementation

### ViewState
Simplified from the original XML states:
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
- `LinearProgressIndicator` at top of screen for page loading
- `@Stable` / `@Immutable` annotations on ViewState for Compose stability
- `key = { _, movie -> movie.id }` in `itemsIndexed` to prevent duplicate key crashes
- `remember(state, searchQuery)` for filtered movie list to avoid recalculation on every recomposition
- `windowInsetsPadding(WindowInsets.statusBars)` to avoid content going under status bar
- `Column` as root container with `SearchBar` fixed at top and `LazyColumn` below

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

- Triggers on every PR regardless of branches
- Cancels in-progress runs when new commit is pushed (`cancel-in-progress: true`)
- Jobs:
  1. `check` — Detekt + Spotless (runs first)
  2. `build` — `assembleDebug` (runs after check)
  3. `tests` — `testAll` (runs after check, parallel to build)

---

## Git Hooks

File: `config/git-hooks/commit-msg`

Install: `./gradlew installGitHooks`

### Commit format
```
[Task - 123] your commit message here
```

---

## API Configuration

```
Base URL:  https://api.themoviedb.org/3/movie/ or BuildConfig.SERVER_ENDPOINT
API Key:   BuildConfig.SERVER_API_KEY
```

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
| Lifecycle | 2.10.0 |
| KSP | 2.3.5 |

---

## Common Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Tests
./gradlew testAll
./gradlew :app:testDebugUnitTest

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

---

## Pending Migrations

- [x] Migrate Home screen to Jetpack Compose
- [ ] Migrate Details screen to Jetpack Compose
- [ ] Migrate Navigation to Navigation Compose (partially done — Compose flow uses Navigation Compose, XML flow still uses Safe Args pending Details migration)
- [ ] Implement search against TMDB API (`/search/movie` endpoint) with debounce (300ms) instead of local filtering — current local search only finds movies already loaded in memory
- [ ] Migrate tests to JUnit 5 with `android-junit5` (Mannodermaus)
- [ ] Remove Robolectric dependency
- [ ] Add Detekt JUnit 5 rules plugin
- [ ] Set up GitHub Actions CI/CD pipelines (PR validation + deploy)
- [x] Restructure project defining Koin modules on its module instead of app
- [x] Add Compose + Navigation Compose setup
- [x] Add detekt-rules-compose with Compose rules in detekt.yml
- [x] Apply kotlinx-serialization plugin via CommonSetupPlugin to all modules
- [ ] Evaluate Arrow dependency (keep or remove)
- [ ] Introduce dedicated mapper classes (currently using extension functions)
- [ ] Remove Glide dependency once all screens are migrated to Compose (replaced by Coil)
- [ ] Remove XML navigation, fragments and related dependencies once Details screen is migrated
