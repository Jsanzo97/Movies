# 🎬 Movies

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-grey?style=flat&logo=kotlin&logoColor=white&labelColor=7F52FF)
![Android](https://img.shields.io/badge/Android-SDK%2036-grey?style=flat&logo=android&logoColor=white&labelColor=green)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.02.00-grey?style=flat&logo=jetpackcompose&logoColor=white&labelColor=blue)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-grey?style=flat&labelColor=green)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-grey?style=flat&logo=githubactions&logoColor=white&labelColor=yellow)
![Firebase](https://img.shields.io/badge/Firebase-Crashlytics%20%2B%20Analytics-grey?style=flat&logo=firebase&logoColor=white&labelColor=orange)
![JUnit5](https://img.shields.io/badge/JUnit5-5.11.0-grey?style=flat&logo=junit5&logoColor=white&labelColor=green)
![JaCoCo](https://img.shields.io/badge/JaCoCo-0.8.12-grey?style=flat&labelColor=green)
[![Coverage](https://img.shields.io/codecov/c/github/Jsanzo97/Movies/develop?style=flat&logo=codecov&logoColor=white&labelColor=f01f7a&color=grey)](https://codecov.io/gh/Jsanzo97/Movies)
![License](https://img.shields.io/badge/License-MIT-grey?style=flat&labelColor=yellow)

Android application that lists and displays movie details using [The Movie Database (TMDB) API](https://www.themoviedb.org/). Built as a reference project to showcase modern Android architecture and engineering practices.

---

## 🏛️ Architecture

The project follows **Clean Architecture** with an **MVVM** presentation pattern, organized into independent Gradle modules:

```
app      → domain
domain   → data
data     → remote, database
build-logic → (no dependencies)
```

| Module | Responsibility |
|---|---|
| `:app` | UI layer — Compose screens, ViewModels, navigation |
| `:domain` | Business logic — UseCases, entities, repository interfaces |
| `:data` | Repository implementations orchestrating remote and local sources |
| `:remote` | Retrofit API, remote DTOs |
| `:database` | Room database, DAOs, local entities |
| `:build-logic` | Convention plugins for shared Gradle configuration |

### Presentation layer
ViewModels expose `StateFlow<ViewState>` with sealed classes per screen (`Loading`, `ScreenSuccess`, `ScreenError`). Navigation side effects are handled via `SharedFlow` to avoid encoding navigation state into the ViewState.

### Domain layer
Zero Android dependencies. UseCases follow a single-responsibility pattern with `suspend operator fun invoke()`. Error handling uses Arrow's `Either` type throughout the data flow.

### Data layer
Repository implementations orchestrate remote and local datastores with a cache-first fallback strategy — if the remote call fails, the last locally stored data is returned.

---

## 🛠️ Tech Stack

### UI
- **Jetpack Compose** — fully migrated from XML/Fragments
- **Navigation Compose** — type-safe navigation using `@Serializable` destinations
- **Coil** — async image loading with loading/error states
- **Material 3** — theming with light/dark mode support via `isSystemInDarkTheme()`
- **Accompanist** — system UI controller and permissions

### Networking & persistence
- **Retrofit 3 + OkHttp 5** — REST client with Kotlinx Serialization converter
- **Room 2.8** — local persistence with KSP-generated DAOs
- **Kotlinx Serialization** — JSON parsing across all modules

### Dependency injection
- **Koin 4 + Koin Annotations** — annotation-based DI with `@KoinViewModel`, `@Single`, and `@ComponentScan`. Each Gradle module defines its own Koin module, aggregated at the `:app` level.

### Async
- **Coroutines + Flow + StateFlow** — structured concurrency throughout. `StandardTestDispatcher` for deterministic test execution.

### Error handling
- **Arrow** (`Either`, `Option`) — functional error handling in repository and data layers.

---

## ⚙️ Build System

The project uses a **custom Gradle plugin system** via an included `build-logic` build with convention plugins:

| Plugin | Responsibility |
|---|---|
| `setup-android-application` | Base `:app` config — Compose, BuildConfig, build types, desugaring, Firebase dependencies |
| `setup-android-library` | Base Android library config for all other modules |
| `common-setup` | Detekt, Spotless/KtLint, JUnit 5, Koin Annotations, Kotlinx Serialization, JaCoCo |

This avoids duplicating Gradle configuration across modules. Adding a new module requires only applying the relevant convention plugin.

### Key build decisions
- **Compose compiler plugin** applied via `apply false` in root + explicit apply in `:app` to avoid `org.jetbrains:annotations` classpath conflicts with AGP 9.0.1 + Kotlin 2.x in included builds
- **KSP** used for Room and Koin Annotations code generation
- **Configuration cache** and **build cache** enabled globally via `gradle.properties`

---

## 🚀 CI/CD

Two GitHub Actions workflows:

```
PR:      check (Detekt + Spotless) → build-and-test (assembleDebug + testAll + jacocoAll + Codecov)
develop: coverage (jacocoAll + Codecov)
```

| Optimization | Detail |
|---|---|
| Gradle cache | `gradle/actions/setup-gradle@v4` + `cache: 'gradle'` on `setup-java` |
| Headless JVM | `JAVA_TOOL_OPTIONS: -Djava.awt.headless=true` suppresses KSP AWT errors |
| Configuration cache | Enabled globally, persisted between runs |
| Single build+test job | Avoids spinning up two runners for tasks that share the same cache |
| Separate coverage workflow | Avoids re-running full pipeline on develop after merge |

**Approximate times:** `check` ~1 min · `build-and-test` ~2 min · `coverage` <1 min after cache has been written

### Secrets
All sensitive values are stored as GitHub Actions Secrets — never hardcoded:

| Secret | Usage |
|---|---|
| `SERVER_API_KEY` | TMDB API key, injected via `BuildConfig` |
| `SERVER_ENDPOINT` | TMDB base URL, injected via `BuildConfig` |
| `GOOGLE_SERVICES_JSON` | Base64-encoded `google-services.json`, decoded before build |
| `CODECOV_TOKEN` | Codecov upload token for coverage reporting |

---

## 🔥 Firebase

- **Crashlytics** — automatic crash reporting in both debug and release builds
- **Analytics** — custom event tracking via a `Tracker` interface implemented by `FirebaseTracker`

ViewModels depend on `Tracker`, not `FirebaseTracker`, keeping them testable without the Firebase SDK. The Firebase implementation is wired via Koin in `AppModule`.

Debug Analytics events can be monitored in Firebase DebugView using Gradle tasks:
```bash
./gradlew enableFirebaseDebug
./gradlew disableFirebaseDebug
```

---

## 🧪 Testing

| Library | Usage |
|---|---|
| JUnit 5 (Jupiter) | Test runner via `android-junit5` |
| MockK | Mocking with `coEvery`, `coVerify`, `relaxed` mocks |
| Kotest | Assertions — `shouldBe`, `shouldBeInstanceOf` |
| Coroutines Test | `runTest` + `StandardTestDispatcher` for deterministic coroutine execution |

ViewModel tests use `Dispatchers.setMain(testDispatcher)` + `advanceUntilIdle()` to control coroutine execution deterministically.

Coverage is measured with **JaCoCo 0.8.12** and reported to [Codecov](https://app.codecov.io/github/jsanzo97/movies) on every PR.

```bash
./gradlew jacocoAll                     # generate coverage reports for all modules
./gradlew :app:jacocoDebugTestReport    # specific module
```

---

## 🔍 Static Analysis

- **Detekt** with `detekt-rules-compose` for Compose-specific linting
- **Spotless** with KtLint for code formatting
- Both wired to the `check` task and enforced on every PR

```bash
./gradlew detektAll       # run Detekt on all modules
./gradlew check           # Detekt + Spotless + tests
```

---

## 🌐 API

Powered by [The Movie Database API v3](https://developer.themoviedb.org/docs).

```
Base URL: https://api.themoviedb.org/3/movie/
Images:   https://image.tmdb.org/t/p/original
```

API key and base URL are injected at build time via `BuildConfig` fields, read from `local.properties` locally and from GitHub Secrets in CI.
