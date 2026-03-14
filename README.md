# 🎬 Movies

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-grey?style=flat&logo=kotlin&logoColor=white&labelColor=7F52FF)
![Android](https://img.shields.io/badge/Android-SDK%2036-grey?style=flat&logo=android&logoColor=white&labelColor=green)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.03.00-grey?style=flat&logo=jetpackcompose&logoColor=white&labelColor=blue)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-grey?style=flat&labelColor=green)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-grey?style=flat&logo=githubactions&logoColor=white&labelColor=yellow)
![Firebase](https://img.shields.io/badge/Firebase-Crashlytics%20%2B%20Analytics%20%2B%20RemoteConfig-grey?style=flat&logo=firebase&logoColor=white&labelColor=orange)
![JUnit5](https://img.shields.io/badge/JUnit5-1.3.0-grey?style=flat&logo=junit5&logoColor=white&labelColor=green)
![JaCoCo](https://img.shields.io/badge/JaCoCo-0.8.12-grey?style=flat&labelColor=green)
![Develocity](https://img.shields.io/badge/Develocity-4.3.2-grey?style=flat&logo=gradle&logoColor=white&labelColor=blue)
![LeakCanary](https://img.shields.io/badge/LeakCanary-2.14-grey?style=flat&logo=square&logoColor=white&labelColor=yellow)
[![Coverage](https://img.shields.io/codecov/c/github/Jsanzo97/Movies/develop?style=flat&logo=codecov&logoColor=white&labelColor=f01f7a&color=grey)](https://codecov.io/gh/Jsanzo97/Movies)
![License](https://img.shields.io/badge/License-MIT-grey?style=flat&labelColor=yellow)

Android application that lists and displays movie details using [The Movie Database (TMDB) API](https://www.themoviedb.org/). Built as a reference project to showcase modern Android architecture and engineering practices.

---

## 🏛️ Architecture

The project follows **Clean Architecture** with an **MVVM** presentation pattern, organized into independent Gradle modules:

```
app      → domain
domain   → data
data     → remote, database, datastore
build-logic → (no dependencies)
```

| Module | Responsibility |
|---|---|
| `:app` | UI layer — Compose screens, ViewModels, navigation |
| `:domain` | Business logic — UseCases, entities, repository interfaces |
| `:data` | Repository implementations orchestrating remote and local sources |
| `:datastore` | DataStore Preferences — persists user preferences |
| `:remote` | Retrofit API, remote DTOs, Firebase Remote Config |
| `:database` | Room database, DAOs, local entities |
| `:build-logic` | Convention plugins for shared Gradle configuration |

### Presentation layer
ViewModels expose `StateFlow<ViewState>` with sealed classes per screen (`Loading`, `ScreenSuccess`, `ScreenError`). Navigation side effects are handled via `SharedFlow` to avoid encoding navigation state into the ViewState.

### Domain layer
Zero Android dependencies. UseCases follow a single-responsibility pattern with `suspend operator fun invoke()`. Error handling uses Arrow's `Either` and `Option` type throughout the data flow.

### Data layer
Repository implementations orchestrate remote and local datastores with a cache-first fallback strategy — if the remote call fails, the last locally stored data is returned.

### Remote layer
Remote datastores implementations to retrieve the information from the API with the `NetworkHandler` to manage all the requests

### Database layer
Dao and database implementations to persist the data locally. Only movies clicked to see its details are stored in local database

---

## 🛠️ Tech Stack

### UI
- **Jetpack Compose** — fully migrated from XML/Fragments
- **Navigation3 1.0.1** — migrated from Navigation Compose. `NavDisplay` + `rememberNavBackStack`, no `NavController`. Slide animations via `transitionSpec`/`popTransitionSpec`. `android:enableOnBackInvokedCallback="true"` required in the manifest for back gesture animations to work correctly on Android 14+.
- **Coil** — async image loading with loading/error states
- **Material 3** — full M3 color scheme generated from seed `#1B4B8A`, dynamic color on Android 12+, explicit typography scale
- **Lottie** — animated splash screen and force update screen with JSON animations
- **Accompanist** — permissions

### Networking & persistence
- **Retrofit 3 + OkHttp 5** — REST client with Kotlinx Serialization converter
- **Room 2.8** — local persistence with KSP-generated DAOs
- **DataStore Preferences** — persists user preferences across sessions
- **Kotlinx Serialization** — JSON parsing across all modules

### Dependency injection
- **Koin 4 + Koin Annotations** — annotation-based DI with `@KoinViewModel`, `@Single`, and `@ComponentScan`. Each Gradle module defines its own Koin module, aggregated at the `:app` level.

### Async
- **Coroutines + Flow + StateFlow** — structured concurrency throughout. `StandardTestDispatcher` for deterministic test execution.

### Error handling
- **Arrow** (`Either`, `Option`) — functional error handling in repository and data layers.

### Performance & Debugging
- **Develocity** — deep build insights, build scans, and caching optimization for both local and CI environments.
- **LeakCanary** — automated memory leak detection in debug builds.

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
- **Develocity Build Scans** enabled for all CI runs and optional for local builds to analyze performance bottlenecks.

---

## ♿ Accessibility

All screens implement Compose semantics for TalkBack and other assistive technologies:

- `paneTitle` on the root `Surface` of every screen — TalkBack announces the screen name on navigation
- `heading()` on title and section header `Text` composables — allows users to navigate by headings
- `mergeDescendants = true` on composite elements (e.g. movie cards, empty search state) — TalkBack reads them as a single unit
- `contentDescription` on loading indicators and icon-only elements
- `Role.Button` on clickable non-button elements (e.g. the homepage link in `DetailsScreen`)
- Decorative images adjacent to a title use `contentDescription = null`

---

## 🌐 RTL Support

Navigation slide animations adapt to the system layout direction via `LocalLayoutDirection`. A `directionMultiplier` captured outside the `transitionSpec` lambdas mirrors all slide directions in RTL locales. Row-based layouts (`MovieItem`, `DetailsContent` header) invert automatically via Compose's built-in RTL support. Label/value pairs use an explicit `Spacer(4.dp)` between the two `Text` elements instead of embedding the space in the label string, so the `:` separator always stays visually attached to the label in both directions.

---

## 🔍 Movie Search

The home screen `SearchBar` triggers a real API search via the `/search/movie` endpoint rather than filtering the already-loaded list. A 500ms debounce ensures the API is only called once the user stops typing. Any in-flight search is cancelled via `currentJob?.cancel()` before launching a new one. Clearing the search restores the paginated list without any extra API call. If the network call fails, the search falls back to a local Room query (`LIKE '%query%'`) over movies the user has previously visited in details. Pagination is paused while a search query is active.

When a search returns no results, an empty state is shown with a themed icon and a descriptive text.

---

## 🖌️ Layout Mode

The home screen supports three grid densities: **Grid2**, **Grid3**, and **Grid4** (columns). The selected layout is persisted via **DataStore Preferences** and restored on every launch. Transitions between layouts are animated via `AnimatedContent`.

---

## 🚀 CI/CD

Two GitHub Actions workflows:

```
PR:      check (Detekt + Spotless) → stability-check (stabilityCheck) + build-and-test (assembleDebug + jacocoMergedCoverageVerification + Codecov)
develop: coverage (jacocoMergedReport + Codecov)
```
**Quality Gate**: Code coverage from merged report must be over 95%. Compose stability baseline must not regress.

| Optimization | Detail |
|---|---|
| Gradle cache | `gradle/actions/setup-gradle@v4` + `cache: 'gradle'` on `setup-java` |
| Develocity | Automatic build scans for CI runs provided by `setup-gradle@v4` |
| Headless JVM | `JAVA_TOOL_OPTIONS: -Djava.awt.headless=true` suppresses KSP AWT errors |
| Configuration cache | Enabled globally, persisted between runs |
| Parallel jobs | `stability-check` and `build-and-test` run in parallel after `check` |
| Separate coverage workflow | Avoids re-running full pipeline on develop after merge |
| Codecov PR comments | Disabled via `comment: false` |

**Approximate times (after Gradle cache is written):** `check` ~1 min · `stability-check` ~1 min · `build-and-test` ~2 min · `coverage` <1 min

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
- **Analytics** — custom event tracking via a `MovieTracker` interface implemented by `FirebaseTracker`
- **Remote Config** — enforces a minimum app version. On every launch the app fetches `min_version` from Remote Config (no cache, `minimumFetchIntervalInSeconds = 0`) and compares it against the current version using semantic versioning. If the app is outdated, the user is redirected to `ForceUpdateScreen` and cannot proceed. On fetch error, the user is let through — fail open strategy.

ViewModels depend on `MovieTracker`, not `FirebaseTracker`, keeping them testable without the Firebase SDK. The Firebase implementation is wired via Koin in `AppModule`.

Debug Analytics events can be monitored in Firebase DebugView using Gradle tasks:
```bash
./gradlew enableFirebaseDebug
./gradlew disableFirebaseDebug
```

---

## 🚀 Force Update Screen

Shown when the app version is below `minVersion` from Remote Config. The user cannot navigate back — `Splash` is removed from the backstack before `ForceUpdate` is pushed.

Displays a looping Lottie rocket animation themed to the app's color palette, with an "Update" button that deep-links to the Play Store preventing a crash if the Play Store is not available on the device.

---

## 🎬 Splash Screen

Animated splash using Lottie — no Android SplashScreen API. The manifest applies a standard system theme (`android:Theme.Material.Light.NoActionBar`) after removing translucent window properties to fix issues with screen rotation.

`LottieComposition` is loaded once in `SplashScreen` and passed down to `SplashContent` to avoid loading it twice. `SplashContent` accepts `composition` and `progress` as parameters, making it previewable without a ViewModel by passing `composition = null`.

**Flow:** animation plays once → on end, `SplashViewModel` fetches Remote Config → navigates to `Home` or `ForceUpdateScreen`.

---

## 🧪 Testing

| Library | Usage |
|---|---|
| JUnit 5 (Jupiter) | Test runner via `android-junit5` |
| MockK | Mocking with `coEvery`, `coVerify`, `relaxed` mocks |
| Kotest | Assertions — `shouldBe`, `shouldBeInstanceOf` |
| Coroutines Test | `runTest` + `StandardTestDispatcher` for deterministic coroutine execution |
| Turbine | `StateFlow` / `Flow` assertions via `.test {}`, `awaitItem()` |

Tests will follow the structured naming `Given-When-Then`

ViewModel tests use `Dispatchers.setMain(testDispatcher)` to control coroutine execution deterministically. `StateFlow` and `Flow` emissions are asserted with **Turbine** — the `.test {}` block subscribes to the flow and `awaitItem()` suspends until the next emission arrives.

Two scheduler helpers control when coroutines run:
- **`advanceUntilIdle()`** — runs all pending coroutines to completion, including time-based delays. Used after triggering an action that launches a coroutine (e.g. `getMovies()`, `saveMovie()`) to let it complete before asserting the resulting state.
- **`runCurrent()`** — executes only the coroutines already queued at the current virtual time, without advancing the clock. Used when testing concurrent guards (e.g. `isLoadingPage`) where the first coroutine must remain suspended while the second call arrives — advancing time would complete the first job and make the guard invisible to the test.

Tests that exercise the search debounce combine both: `advanceTimeBy(501)` advances the virtual clock past the 500ms window, then `advanceUntilIdle()` completes the resulting coroutine.

`StateFlow` instances using `WhileSubscribed` require an active collector to start the upstream. The Turbine `.test {}` block acts as that collector automatically — no manual `collect {}` job is needed.

Coverage is measured with **JaCoCo 0.8.12** and reported to [Codecov](https://app.codecov.io/github/jsanzo97/movies) on every PR.
```bash
./gradlew jacocoMergedReport                # generate merged coverage report for all modules
./gradlew jacocoAll                         # generate individual reports per module
./gradlew :app:jacocoDebugTestReport        # specific module
./gradlew jacocoMergedCoverageVerification  # verify merged coverage is over 95%
```

## 📱 Compose Previews
Screens use a custom `@PreviewOnDevices` multipreview annotation to provide small, medium and large devices preview with rtl support

---

## 🔍 Static Analysis

- **Detekt** with `detekt-rules-compose` for Compose-specific linting
- **Spotless** with KtLint for code formatting
- **Compose Stability Analyzer** — Gradle plugin that generates a composable stability report and compares it against a committed baseline on every PR. Any new UNSTABLE composable that is not in the baseline fails the CI build. Locally it only warns. Baseline in `app/stability/`.
- Both Detekt and Spotless wired to the `check` task and enforced on every PR

```bash
./gradlew detektAll       # run Detekt on all modules
./gradlew check           # Detekt + Spotless + tests
./gradlew stabilityDump   # regenerate Compose stability baseline (commit the result)
./gradlew stabilityCheck  # verify no stability regressions against baseline
```

---

## 🌐 API

Powered by [The Movie Database API v3](https://developer.themoviedb.org/docs).

```
Base URL: https://api.themoviedb.org/3/movie/
Images:   https://image.tmdb.org/t/p/original
Movies:   GET movie/popular
Details:  GET movie/{movie_id}
Search:   GET search/movie?query=...
```

API key and base URL are injected at build time via `BuildConfig` fields, read from `local.properties` locally and from GitHub Secrets in CI.