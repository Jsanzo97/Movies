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
| UI | XML Views + Fragments | Migrating to Compose (screen by screen) |
| Architecture | MVVM + Clean Architecture | |
| DI | Koin 4.1.1 | Using Koin Annotations for DI |
| Navigation | Jetpack Navigation Component + Safe Args | Migrating to Navigation Compose |
| Networking | Retrofit 3.0.0 + OkHttp 5.3.2 | |
| Serialization | Kotlinx Serialization 1.10.0 | |
| Database | Room 2.8.4 | |
| Async | Coroutines 1.10.2 + Flow + StateFlow | |
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
```kotlin
fun <L, R> Either<L, R>.onSuccess(action: (R) -> Unit): Either<L, R>
fun <L, R> Either<L, R>.onError(action: (L) -> Unit): Either<L, R>
fun <T> Option<T>.onSome(action: (T) -> Unit): Option<T>
fun <T> Option<T>.onNone(action: () -> Unit): Option<T>
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
- Fragments observe StateFlow.
- New Compose screens live in `ui/compose/` (parallel to existing Fragments during migration).

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
"debugImplementation"(libs().getLibrary("compose-ui-tooling"))
```

### Compose screen structure in `:app`
```
app/src/main/kotlin/jsanzo/movies/
└── ui/
    └── compose/
        ├── ComposeActivity.kt
        └── navigation/
        │   ├── AppDestinations.kt
        │   └── AppNavigation.kt
        └── screens/
            ├── home/
            │   └── HomeScreen.kt
            └── details/
                └── DetailsScreen.kt
```

### Navigation
Type-safe Navigation Compose using `@Serializable` data objects:
```kotlin
sealed interface AppDestinations {
    @Serializable data object Home : AppDestinations
    @Serializable data object Details : AppDestinations
}
```

`ComposeActivity` is registered in `AndroidManifest.xml` with `android:exported="false"`. It runs parallel to the existing XML/Fragment flow during migration.

---

## Dependency Injection — Koin Annotations

The project uses Koin Annotations for dependency injection, promoting a modular and decentralized approach where each Gradle module is responsible for its own dependency providers.

Koin is initialized in `MoviesApplication.onCreate()` by loading a single, aggregated `AppModule`.

```kotlin
// In MoviesApplication.kt
startKoin {
    androidLogger()
    androidContext(this@MoviesApplication)
    modules(AppModule().module) // .module is generated by Koin KSP
}
```

### Koin Module Structure

Each feature or layer module defines its own Koin module using the `@Module` annotation. The `@ComponentScan` annotation is used to automatically scan for injectable components within the module's package.

| Koin Module | Location | Purpose |
|---|---|---|
| `AppModule` | `:app/di` | Main module, includes all other modules. |
| `DataModule` | `:data/di` | Provides repository implementations. |
| `DatabaseModule`| `:database/di`| Provides Room DB, DAOs, and the local datastore. |
| `RemoteModule` | `:remote/di` | Provides the remote datastore (`MoviesService`). |
| `NetworkModule`| `:remote/di` | Provides Retrofit and OkHttp dependencies. |
| `AppRemoteModule`| `:remote/di/`| Provides build-variant specific network config (e.g., Chucker). |
| `DomainModule` | `:domain/di` | Provides UseCases. |
| `HomeModule` | `:app/di/home`| Provides Home screen ViewModel and its UseCases. |
| `DetailsModule`| `:app/di/details`| Provides Details screen ViewModel and its UseCases. |

---

## Navigation

Currently uses Jetpack Navigation Component with Safe Args for the existing XML/Fragment flow. Navigation is managed via `NavigationManagerViewModel`.

New Compose flow uses Navigation Compose with type-safe destinations (`AppDestinations`).

**Planned**: Migrate all screens to Navigation Compose (screen by screen).

Current screens:
- `HomeFragment` → `DetailsFragment` (via `actionHomeFragmentToDetailsFragment(movieId)`)
- `HomeScreen` → `DetailsScreen` (Compose, via `AppNavigation`)

---

## Build Logic (`build-logic` module)

Convention plugins are defined in `build-logic/src/main/kotlin/`.

### Available Plugins
| Plugin ID | Class | Purpose |
|---|---|---|
| `setup-android-application` | `SetupAndroidApplicationPlugin` | Base setup for the `:app` module. Includes Compose config. |
| `setup-android-library` | `SetupAndroidLibraryPlugin` | Base setup for Android library modules. |
| `common-setup` | `CommonSetupPlugin` | Applies Detekt, Koin, and other common configurations. |

### Namespace Auto-calculation

```kotlin
internal fun Project.calculateNamespace(): String {
    val packageName = path.removePrefix(":").split("-", ":").joinToString(".")
    return if (path == ":app") "jsanzo.movies" else "jsanzo.movies.$packageName"
}
```

### Version helpers
```kotlin
internal fun VersionCatalog.getVersion(version: String): Int =
    findVersion(version).get().requiredVersion.toInt()
```

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

| Library | Usage                                         |
|---|-----------------------------------------------|
| JUnit 5 (Jupiter) | Test runner via `android-junit5` plugin       |
| MockK | Mocking (`mockk()`, `coEvery`, `coVerify`)    |
| Kotest | Assertions (`shouldBe`, `shouldBeInstanceOf`) |
| Coroutines Test | `runTest`, `UnconfinedTestDispatcher`         |

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
./gradlew :app:testDebugUnitTest           # Specific module (shows Test Results panel in AS)
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
- Jobs run in parallel after `check` passes:
  1. `check` — Detekt + Spotless (runs first)
  2. `build` — `assembleDebug` (runs after check)
  3. `tests` — `testAll` (runs after check, parallel to build)

All three jobs are required status checks before merging.

---

## Git Hooks

File: `config/git-hooks/commit-msg`

Install with:
```bash
git config core.hooksPath config/git-hooks
```

Or via Gradle:
```bash
./gradlew installGitHooks
```

### Hook behavior
- Validates branch name starts with `feature/`, `refactor/`, `bugfix/`, or `hotfix/`
- Validates commit message is not empty and has minimum 10 characters
- If commit doesn't start with `[Task - NUMBER]`, auto-increments from last task number in git log
- Ignores automatic git commits (Merge, Rebase, fixup!, squash!)

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
Both are configured as `buildConfigField` in `SetupAndroidApplicationPlugin` and should not be hardcoded.

---

## Version Catalog

All dependencies managed via `gradle/libs.versions.toml`.

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
| Navigation | 2.9.7 |
| Navigation Compose | 2.9.0 |
| Compose BOM | 2025.06.00 |
| Arrow | 2.2.1.1 |
| Detekt | 1.23.8 |
| detekt-rules-compose | 0.4.27 |
| Lifecycle | 2.10.0 |
| KSP | 2.3.5 |
| versionMajor | 1 |
| versionMinor | 0 |
| versionPatch | 0 |

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

- [ ] Migrate UI to Jetpack Compose (screen by screen) — HomeScreen next
- [ ] Migrate Navigation to Navigation Compose
- [ ] Migrate tests to JUnit 5 with `android-junit5` (Mannodermaus)
- [ ] Remove Robolectric dependency
- [ ] Add Detekt JUnit 5 rules plugin
- [ ] Re-add `compiler` and `JUnit` sections to `detekt.yml` with correct plugins
- [ ] Set up GitHub Actions CI/CD pipelines (PR validation + deploy)
- [x] Restructure project defining Koin modules on its module instead of app, unidirectional flow approach
- [x] Add Compose + Navigation Compose setup
- [x] Add detekt-rules-compose with Compose rules in detekt.yml
- [ ] Evaluate Arrow dependency (keep or remove)
- [ ] Introduce dedicated mapper classes (currently using extension functions)