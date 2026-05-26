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

## Back confirmation and color row bug fix

- Add Note now intercepts both system back and the toolbar navigation button.
- If the user has typed a title, typed a description, or changed the note color, the app asks whether to save, discard, or cancel before leaving.
- The save action in the confirmation dialog validates the title and keeps the user on the screen if the title is missing.
- Reduced note color swatch size, spacing, selected stroke width, icon size, and selected-scale animation so the horizontal picker fits much better and avoids the partially clipped look shown in the screenshot.

## Add Note keyboard and editor-size fix

- Enlarged the description editor so it fills the remaining space inside the editor card instead of leaving unused empty space below it.
- Made the editor content scrollable with `fillViewport`, so the screen remains usable on smaller devices and when the keyboard is visible.
- Added `windowSoftInputMode="adjustResize"` to `MainActivity`.
- Added keyboard-inset handling in `AddNoteFragment` so the Save Note button stays pinned above the keyboard instead of being hidden by it.

## Edit Mode + Version History

- Added note editing from the detail screen with a toolbar edit action.
- Added animated transition between read mode and edit mode.
- Added validation for edited titles.
- Added unsaved-change handling when leaving edit mode or pressing back.
- Added Room database version 3 with note timestamps and an `edited` flag.
- Added `note_history_table` to persist every previous note version before an update.
- Added an edit history dialog that lists changed fields, previous content, previous color, and timestamp.
- Added a small edited badge on each changed note in the list.
- Preserved MVVM, XML layouts, and no DI framework.

## Animated keyboard-aware Save button refinement

- Kept the previous keyboard-aware Save Note behavior instead of removing it.
- Replaced the instant bottom-margin jump with a smooth animated transition when the keyboard opens or closes.
- Added a subtle Save button scale animation so the pinned bottom action feels intentional and modern.
- Added focused-field auto-scroll assistance so the active title/description field stays easier to see while typing.


## AdMob implementation
- Added Google Mobile Ads SDK dependency using the current official `play-services-ads` artifact.
- Added test AdMob App ID and banner unit ID. Replace both before production release.
- Added app-level SDK initialization in `TetrakApplication`.
- Added lifecycle-safe adaptive banner loading in `MainActivity`.
- Added animated banner container UI with loading state and automatic hide-on-keyboard behavior.
- Added localized Ads information card in Settings.

## AdMob save-count video/interstitial ads
- Added an interstitial video-style AdMob flow using Google's official test interstitial ad unit.
- Video/interstitial ads are preloaded on app start and after each dismissal/failure.
- A full-screen ad is considered only after every 3 saved notes and only after a 2-minute cooldown.
- Saving a note is never blocked by ads. If the ad is not ready or fails, the app continues normally.
- The ad is shown at a natural break after the note is saved and before returning to the notes list.
- Production releases must replace both banner and interstitial test ad unit IDs in `AdMobConfig`.

## Edit screen keyboard-aware save action
- Matched the Edit Note screen behavior with the Add Note screen keyboard handling.
- The bottom edit action row now animates above the keyboard using IME insets instead of being covered by the keyboard.
- Added the same polished Save button scale feedback used on the create-note screen.
- Added focused edit-field auto-scroll while typing, so the title/description editor remains visible when the keyboard is open.

## Instrumental note editor tools
- Added a compact modern editor tools card to Add Note and edit mode in Note Details.
- Added Copy behavior that copies the selected text when there is a selection, otherwise copies the active field or the whole note draft.
- Added in-note search with an animated inline search bar and next-match selection across title and description.
- Added Undo and Redo support for title/description editing with live enabled/disabled button state.
- Added small tap/scale animations for the editor tools so the editor feels more polished and productive.
- Kept everything XML-based, MVVM-friendly, dependency-free, and compatible with the existing keyboard-aware save behavior.

## Editor workspace refinement
- Rebalanced the Add Note and Edit Note layouts so the writing area is the primary workspace again.
- Hid the secondary subtitle on Add Note to recover vertical editor space.
- Reduced color picker height, margins, and editor tool spacing.
- Converted editor tools to compact icon-only buttons with content descriptions and tooltips.
- Increased Add Note description editor minimum height.
- Increased Edit Note description editor height for a normal writing experience.

## Gemini AI title generation
- Added a Gemini REST client (`GeminiTitleGenerator`) for generating a short title from the note description.
- Added AI title action as the end icon of the title field in both Add Note and Edit Note flows.
- The prompt keeps the generated title short, max 5 words, and uses the same language as the description.
- Added localized UI/error strings for English, Armenian, Russian, Arabic, and Persian.
- API configuration is read from `local.properties` via `BuildConfig`:
  - `GEMINI_API_KEY=your_key_here`
  - `GEMINI_MODEL=gemini-2.0-flash`
- For production, move the API key behind a backend/serverless proxy before publishing, because APKs can be decompiled.
