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


## Settings, Localization, and Color Picker Update

- Added a `SettingsFragment` using XML + MVVM with `SettingsViewModel`.
- Added global theme selection: system, light, and dark.
- Added runtime language selection using AppCompat locales: English, Armenian, Russian, Arabic, and Persian.
- Added localized `strings.xml` resources for `values-hy`, `values-ru`, `values-ar`, and `values-fa`.
- Added note color support with an animated horizontal color picker.
- Added a Room `1 -> 2` migration that preserves existing notes and assigns them the default accent color.
- Removed separate bottom back buttons from add/detail screens and moved navigation to MaterialToolbar top-left navigation icons.
- Added settings navigation from the start screen header.

## Custom color picker update

- Replaced the fixed horizontal note color palette with a modern custom color picker dialog.
- Added `AdvancedColorPickerView`, a reusable XML-compatible custom View that supports full HSV color selection.
- Users can now choose any note color by dragging on the saturation/value spectrum, adjusting hue, or typing a hex color.
- Added animated color preview feedback and validation for invalid hex input.
- Kept the implementation dependency-free: no third-party color picker library, no DI, and no Compose.

## Hybrid preset + custom color picker update

- Restored the fast horizontal preset color picker for common note colors.
- Kept the advanced custom color picker dialog for choosing any color.
- Added a compact trailing custom color item inside the preset picker; tapping it opens the advanced dialog.
- The custom item shows a spectrum when no custom color is selected, and it becomes the selected swatch when the chosen color is outside the preset palette.
- Kept XML UI, MVVM, no DI, and no third-party color picker dependency.
