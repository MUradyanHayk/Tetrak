# Tetrak modernization notes

## Kept as requested
- MVVM architecture is preserved.
- XML layouts are preserved; no Compose was added.
- No DI framework was added.

## Fixed
- Removed global `MainActivity` navigation access and replaced it with `findNavController()`.
- Removed global repository storage and initialized repositories inside AndroidViewModels using the application context.
- Fixed repository callbacks by returning to the main thread after Room writes complete.
- Fixed the RecyclerView adapter by replacing `notifyDataSetChanged()` with `ListAdapter` + `DiffUtil`.
- Fixed Room query ordering so newest notes appear first.
- Fixed view binding lifecycle leaks in fragments by clearing bindings in `onDestroyView()`.
- Replaced the deprecated `<fragment>` host with `FragmentContainerView`.
- Replaced the old CardView item with MaterialCardView to avoid an extra dependency.

## UI / UX modernization
- Material 3 DayNight theme.
- Modern note list header, empty state, cards, extended FAB, editor card, and detail card.
- Light and dark color palettes in `values/colors.xml` and `values-night/colors.xml`.
- Navigation transitions and screen/card/FAB intro animations.
- RecyclerView layout animation and default item animator.
- Better input validation for empty titles.

## Build note
The project wrapper tried to download Gradle `9.3.1`, but this environment has no network access to `services.gradle.org`, so I could not run a full Gradle build here. The Gradle files were still corrected by adding the missing Kotlin Android plugin and KSP declaration.

## AGP 9 Kotlin migration fix

- Removed `alias(libs.plugins.kotlin.android)` from the top-level and app-level Gradle plugin blocks.
- Removed the `kotlin-android` plugin entry and unused Kotlin plugin version from `gradle/libs.versions.toml`.
- Removed the deprecated `android.kotlinOptions {}` block. With AGP 9 built-in Kotlin, Kotlin's JVM target defaults to `android.compileOptions.targetCompatibility`.
- Kept KSP because Room still uses KSP for annotation processing.

