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
| DI | Koin 4.1.1 | |
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
├── movies/          # Application module (com.android.application)
├── common/          # Shared utilities and constants
├── domain/          # UseCases, Entities, Repository interfaces, Errors
├── data/            # Repository implementations, Data entities
├── database/        # Room database, DAOs, DB entities
├── remote/          # Retrofit API, Remote DTOs, Remote data sources
└── build-logic/     # Convention plugins (Gradle build logic)
```

### Dependency Graph

```
movies → domain, data, database, common, remote
data   → domain, database, remote
remote → domain
database → domain
common → (no dependencies)
```

---

## Package Naming Convention

Base package: `jsanzo.movies`

Each module appends its name automatically via `calculateNamespace()` in build-logic:
- `:common` → `jsanzo.movies.common`
- `:domain` → `jsanzo.movies.domain`
- `:data` → `jsanzo.movies.data`
- `:database` → `jsanzo.movies.database`
- `:remote` → `jsanzo.movies.remote`
- `:movies` → `jsanzo.movies` (special case — root app module)

---

## Architecture

### Domain Layer (`:domain`)
- **Entities**: Pure Kotlin data classes (`Movie`, `MovieDetails`, `MovieResult`)
- **UseCases**: Single-responsibility, `suspend operator fun invoke()` pattern
- **Repository interfaces**: Defined here, implemented in `:data`
- **Errors**: Sealed classes/objects (`InvalidParametersError`, `NotFoundError`)
- Zero Android dependencies

#### UseCase pattern
```kotlin
class UseCase(private val repository: Repository) {
    suspend operator fun invoke() = repository.function()
}
```

UseCases delegate directly to the repository and return `Either<Error, T>` or `Flow<T>` from Arrow.

### Data Layer (`:data`)
- Repository implementations orchestrating `:remote` and `:database`
- Own data entities (e.g. `DataMovieResult`) with extension functions for mapping

#### Mapping pattern (extension functions, no dedicated mapper classes)
```kotlin
fun DataMovieResult.toMovieResult() = MovieResult()
fun MovieResult.toDataMovieResult() = DataMovieResult()
```

> Note: Dedicated mapper classes may be introduced in a future refactor.

### Remote Layer (`:remote`)
- Retrofit API interface
- Remote DTOs mapped to domain entities via extension functions
- Base URL: `https://api.themoviedb.org/3/movie/`, available via `BuildConfig.SERVER_ENDPOINT`
- API Key available via `BuildConfig.SERVER_API_KEY`

### Database Layer (`:database`)
- Room database
- DAOs for local persistence
- DB entities mapped to domain entities via extension functions

### Presentation Layer (`:movies`)
- ViewModels expose `StateFlow<ViewState>`
- Sealed classes for ViewState per screen:
    - `HomeViewState`: `InitialState`, `MoviesRetrieved`, `ErrorInOperation`, `SavedMovie`
    - `DetailsViewState`: `InitialState`, `DetailsRetrieved`, `ErrorInOperation`
- Fragments observe StateFlow

---

## Dependency Injection — Koin

Koin is initialized in `MoviesApplication.onCreate()` with the following modules:

| Koin Module | Location | Contents |
|---|---|---|
| `remoteModule` | `:movies/di/remote` | Common remote dependencies |
| `appRemoteModule` | `:movies/di/remote` | App-specific remote (debug/release variants) |
| `localModule` | `:movies/di/local` | Room database, DAOs |
| `dataModule` | `:movies/di/data` | Repository implementations |
| `homeModule` | `:movies/di/home` | Home screen ViewModel |
| `detailsModule` | `:movies/di/details` | Details screen ViewModel |

---

## Navigation

Currently uses Jetpack Navigation Component with Safe Args. Navigation is managed via `NavigationManagerViewModel` which wraps `NavController` with safe navigation to avoid crashes on double-tap or back-stack inconsistencies.

**Planned**: Migrate to Navigation Compose alongside the Compose UI migration (screen by screen).

Current screens:
- `HomeFragment` → `DetailsFragment` (via `actionHomeFragmentToDetailsFragment(movieId)`)

---

## Build Logic (`build-logic` module)

Convention plugins defined in `build-logic/src/main/kotlin/` and registered in `build-logic/build.gradle.kts`.

### Available Plugins

| Plugin ID | Class | Purpose |
|---|---|---|
| `setup-android-application` | `SetupAndroidApplicationPlugin` | compileSdk, minSdk, buildTypes, compileOptions, sourceSets, desugaring, BuildConfig fields |
| `setup-android-library` | `SetupAndroidLibraryPlugin` | compileSdk, minSdk, compileOptions, auto namespace |
| `common-verifications` | `CommonVerificationsPlugin` | Applies Detekt, connects to `check` task |

`common-verifications` is applied internally by both application and library plugins — never apply it manually in a module.

### Namespace Auto-calculation

```kotlin
internal fun Project.calculateNamespace(): String {
    val packageName = path.removePrefix(":").split("-", ":").joinToString(".") {
        if (it == "public") "publicapi" else it
    }
    return if (path == ":movies") "jsanzo.movies" else "jsanzo.movies.$packageName"
}
```

---

## Static Analysis — Detekt

Config file: `config/detekt.yml` at project root. Validation is enabled — unknown properties in the yml will fail the build.

### Gradle Tasks

| Task | Description |
|---|---|
| `./gradlew detektAll` | Runs Detekt on all modules |
| `./gradlew :module:detekt` | Runs Detekt on a specific module |
| `./gradlew check` | All checks including Detekt per module |

### Key Rules

- `CyclomaticComplexMethod` — threshold 15
- `LongMethod` — threshold 60
- `LongParameterList` — threshold 6 (functions), 12 (constructors)
- `TooManyFunctions` — threshold 26
- `UnusedPrivateMember`, `UnusedPrivateProperty` — enabled
- `ForbiddenComment` — TODO, FIXME, STOPSHIP forbidden
- `ReturnCount` — max 2 per function
- `GlobalCoroutineUsage`, `RedundantSuspendModifier`, `SuspendFunWithFlowReturnType` — enabled
- `MagicNumber` — disabled
- `NewLineAtEndOfFile` — enabled

### Detekt Plugins Installed

| Plugin | Artifact | Purpose |
|---|---|---|
| Compose rules | `io.nlopez.compose.rules:detekt:0.4.22` | Compose-specific rules (ready for Compose migration) |

> Sections `compiler` and `JUnit` have been removed from `detekt.yml` pending proper plugin identification. Re-add when migrating to JUnit 5.

---

## Testing

Tests are colocated in the module they test:

```
module/src/test/kotlin/        → Unit tests
module/src/androidTest/kotlin/ → Instrumented tests
```

### Current Setup (migrating to JUnit 5)

| Library | Usage |
|---|---|
| JUnit 4 | Test runner (being replaced) |
| Mockito + mockito-kotlin | Mocking |
| Coroutines Test (`runTest`, `StandardTestDispatcher`) | Async testing |
| Robolectric | Being removed — not needed since ViewModels don't use Android classes directly |

### Target Setup

- JUnit 5 (Jupiter) via `android-junit5` plugin by Mannodermaus
- Mockk + kotest
- Coroutines Test
- No Robolectric

### Run Tests

```bash
./gradlew testAll                          # All modules
./gradlew :movies:testDebugUnitTest        # Specific module
```

---

## API Configuration

```
Base URL:  https://api.themoviedb.org/3/movie/ or BuildConfig.SERVER_ENDPOINT
API Key:   BuildConfig.SERVER_API_KEY
```

Both configured as `buildConfigField` in `SetupAndroidApplicationPlugin`. Never hardcode them.

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

### CI/CD (Planned — GitHub Actions)

- **PR validation pipeline**: Run `check` + `detektAll` + `testAll` on every PR to `develop`
- **Deploy pipeline**: Build and sign release APK/AAB on merge to `main`

> Solo project — no code review process.

---

## Version Catalog

All dependencies managed via `gradle/libs.versions.toml`.

| Library | Version |
|---|---|
| Android Gradle Plugin | 9.0.1 |
| Kotlin | 2.3.10 |
| Coroutines | 1.10.2 |
| Koin | 4.1.1 |
| Retrofit | 3.0.0 |
| OkHttp | 5.3.2 |
| Room | 2.8.4 |
| Navigation | 2.9.7 |
| Arrow | 2.2.1.1 |
| Detekt | 1.23.8 |
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
./gradlew :movies:testDebugUnitTest

# Static analysis
./gradlew detektAll
./gradlew :movies:detekt

# All checks
./gradlew check

# Stop Gradle daemon (Windows file lock workaround)
./gradlew --stop
```

---

## Known Issues & Notes

- **Windows file locking**: Gradle daemon may lock `.jar` files between builds on Windows. Run `./gradlew --stop` if build fails with file access errors. Workaround: `org.gradle.parallel=false` in `gradle.properties`.
- **Detekt config validation**: `config.validation = true` — any unknown section in `detekt.yml` fails the build. Only add rule sections if the corresponding plugin is installed as `detektPlugins`.
- **Namespace special case**: `:movies` module has a hardcoded `if` in `calculateNamespace()` because its namespace is `jsanzo.movies`, not `jsanzo.movies.movies`.

---

## Pending Migrations

- [ ] Migrate UI to Jetpack Compose (screen by screen)
- [ ] Migrate Navigation to Navigation Compose
- [ ] Migrate tests to JUnit 5 with `android-junit5` (Mannodermaus)
- [ ] Remove Robolectric dependency
- [ ] Add Detekt JUnit 5 rules plugin
- [ ] Re-add `compiler` and `JUnit` sections to `detekt.yml` with correct plugins
- [ ] Set up GitHub Actions CI/CD pipelines (PR validation + deploy)
- [ ] Move tests to their respective modules (currently all in `:movies`)
- [ ] Restructure project defining Koin modules on its module instead of app, unidirectional flow approach
- [ ] Evaluate Arrow dependency (keep or remove)
- [ ] Introduce dedicated mapper classes (currently using extension functions)