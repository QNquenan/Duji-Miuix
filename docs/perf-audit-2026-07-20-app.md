# Compose Performance Audit - 2026-07-20 - app

## Scope and outcome

This audit followed Measure -> Diagnose -> Fix -> Verify. The workspace now has a physical API 36 device, so the report retains the release R8 and Macrobenchmark baseline from the initial audit. Runtime UI fixes were applied in this pass without rerunning Macrobenchmark, per the user's request; no new frame-time improvement claim is made.

## Environment

- Module: `:app`
- AGP: `9.2.1`
- Kotlin / Compose Compiler plugin: `2.3.21`
- Compose BOM configured: `2025.02.00`
- Resolved Compose UI/runtime: `1.11.2` (dependency resolution selected this over the BOM request)
- Lifecycle configured: `2.9.4`
- compileSdk / targetSdk / minSdk: `37 / 36 / 33`
- Java: OpenJDK `21.0.9`
- Device: Redmi `2510DRK44C`, device `annibale`, API `36`, 8 cores, 11.66 GB RAM
- Device fingerprint: `Redmi/annibale/annibale:16/BP2A.250605.031.A3/OS3.0.305.0.WPKCNXM:user/release-keys`
- Sustained performance mode: disabled

## Baseline (Phase 1)

- `:app:assembleRelease`: passed with `minifyReleaseWithR8` and resource shrinking.
- Release APK: `4,344,735` bytes in the measured build; R8 mapping and usage outputs were generated.
- Cold startup `timeToInitialDisplayMs`, 10 iterations: `331.85 ms` median with profile vs `382.91 ms` without profile, a `51.07 ms` (`13.3%`) improvement.
- Scroll `FrameTimingMetric`, 5 iterations, with profile: frame CPU P50/P90/P95/P99 = `3.47 / 18.85 / 19.77 / 23.82 ms`; frame overrun P50/P90/P95/P99 = `3.90 / 11.73 / 14.68 / 23.20 ms`.
- Scroll A/B without profile: frame CPU P50/P90/P95/P99 = `5.07 / 32.40 / 33.22 / 41.63 ms`; frame overrun P50/P90/P95/P99 = `16.35 / 34.59 / 35.80 / 37.43 ms`.
- Baseline Profile: generated at `app/src/release/generated/baselineProfiles/baseline-prof.txt` and packaged as `assets/dexopt/baseline.prof` / `baseline.profm`.
- `ReportDrawn` / `ReportDrawnWhen`: not present; startup numbers are `timeToInitialDisplay`, not time to full meaningful display.
- `ReportDrawn` / `ReportDrawnWhen`: not present.

## Diagnosis (Phase 2)

### Compiler report results

Release Compose Compiler reports were generated under `app/build/compose_compiler/` after adding a conditional report destination in `app/build.gradle.kts`. Current Kotlin compiler output contains `app-classes.txt`, `app-composables.txt`, and `app-composables.csv`; it does not emit the `module.json` file described by the older skill template.

- Composable entries: `45`
- Restartable entries: `39`
- Restartable but not skippable: `0`
- Unstable parameter sites: `28`
- Classes reported unstable: `24` of `45`
- Strong Skipping is active through Kotlin `2.3.21`; all restartable entries in this report are also skippable. Skippability is not treated as the performance KPI.

### Confirmed findings, ordered by priority

1. **P1 - scrolling still misses the frame budget after Baseline Profile.** With the profile, frame CPU P90 is `18.85 ms` and frame overrun P90 is `11.73 ms` on the Redmi API 36 device. The profile improves P90 by about `41.8%` versus `CompilationMode.None`, but the remaining positive overrun means the main swipe journey still drops frames.

2. **P1 - lint reports an actual predictive-back bug.** `MainActivity.kt:96-102` calls `PredictiveBackHandler` without collecting its `Flow<BackEventCompat>`. `:app:lintRelease` fails with one error (`NoCollectCallFound`) and 55 warnings. The handler can execute the complete back action at gesture start instead of separating gesture progress from completion.

3. **P1 - animated FAB offset reads state in Composition (fixed in Phase 3).** `MyItemsScreen.kt:339` and `ThoseDaysScreen.kt:459` now use the lambda `Modifier.offset` overload. `fabBottomOffset` is produced by `animateDpAsState`, so its value is read during Layout instead of rebuilding the page subtree during animation.

4. **P1/P2 - the blur/backdrop path is the leading unmeasured scroll suspect.** Blur is enabled by default in `DuJiTheme` (`Theme.kt:22-25`). `MainActivity.kt:108-166` captures the pager backdrop, while `FloatingBottomBar.kt:299-405` applies vibrancy, blur, lens and extra draw layers, including a second transparent content row. `CombinedBackdrop.kt:12-29` draws two backdrops. The current A/B isolates compilation/profile cost, not blur cost; the next measurement should compare blur enabled versus disabled.

5. **P2 - the main list boundaries have unstable collection parameters.** The compiler flags `MyItemsContent.filteredItems: List<ItemCardUiModel>` and `ThoseDaysContent.filteredDays: List<DayCardUiModel>` (`app-composables.txt`), plus `EmojiPickerDialog.emojiOptions: List<EmojiOption>` and the `Set<Int>` parameters of the weekday/month-day dialogs. Lazy lists already have stable `key` and `contentType` declarations in `MyItemsSections.kt:125-128`, `MyItemsSections.kt:160-163`, `ThoseDaysSections.kt:120-123`, and `ThoseDaysSections.kt:148-151`; missing keys are not an issue.

6. **P2 - `DayData` is unstable because it exposes ordinary lists.** `DayModels.kt:22-35` contains `weekDays: List<Int>` and `monthDays: List<Int>`, and the compiler reports both fields unstable. `DayCardUiModel` is annotated `@Immutable` (`UiModels.kt:20-28`) even though its `day` field remains compiler-reported unstable. The annotation should only remain if the immutability contract is true for every reachable field; otherwise it can create incorrect skip assumptions.

7. **P2 - one repository Flow is collected without lifecycle awareness.** `MainActivity.kt:82-84` uses `collectAsState` for settings while screen composables correctly use `collectAsStateWithLifecycle`. This is low frequency, so it is unlikely to explain scroll jank, but it is a straightforward lifecycle/battery cleanup.

8. **P2 - calendar layout has a subcomposition candidate, not a proven hotspot.** `CheckInScreen.kt:266-307` uses `BoxWithConstraints` around a custom calendar layout. The screen also derives the displayed month from pager state at `CheckInScreen.kt:95-97`. This should only be changed after a trace confirms repeated composition/measure cost; the calendar is not a lazy scrolling list.

## Fixes applied (Phase 3)

The initial pass added measurement/release infrastructure, and this pass applied targeted runtime fixes. The existing bottom navigation blur/backdrop/lens/shader path was intentionally left unchanged.

| Skill | Recommended change | Files | Macrobenchmark delta |
| --- | --- | --- | --- |
| `configuring-r8-for-compose` | Enabled R8 full mode, resource shrinking, optimize ProGuard defaults, and debug fallback signing | `app/build.gradle.kts`, `app/proguard-rules.pro` | APK release build passed; APK is about 4.3 MB |
| `generating-baseline-profiles` | Added generator/benchmark module, profileable manifest entry, startup + scroll journey, and generated profile | `settings.gradle.kts`, `build.gradle.kts`, `app`, `baselineprofile` | Startup `382.91 -> 331.85 ms`; scroll P90 `32.40 -> 18.85 ms` |
| `page-transition` | Replaced custom `animateScrollBy` page-distance calculation with `PagerState.animateScrollToPage` so distant navigation does not animate through every intermediate page | `MainActivity.kt` | Not measured |
| `deferring-state-reads` | Changed both animated FAB offsets to the lambda `Modifier.offset` overload | `MyItemsScreen.kt`, `ThoseDaysScreen.kt` | Not measured |
| `collecting-flows-safely` | Changed settings collection to `collectAsStateWithLifecycle` and collected predictive-back progress | `MainActivity.kt` | Not measured |
| `background-data-preparation` | Moved item/day decode, sorting, card-model, statistics, and date-status transformations to `Dispatchers.Default` | `MyItemsViewModel.kt`, `ThoseDaysViewModel.kt` | Not measured |
| `optimizing-lazy-layouts` | Added `contentType = "stats"` to the list and grid statistics headers; retained existing stable item IDs and content types | `MyItemsSections.kt` | Not measured |
| `stabilizing-compose-types` | Not applied; compiler diagnosis retained for the next iteration | `DayModels.kt`, `UiModels.kt`, screen boundaries | Not measured |
| Bottom navigation effects | Intentionally unchanged; blur/backdrop/lens/shader behavior remains as before | `FloatingBottomBar.kt`, liquid components | Not measured |

`beyondBoundsPageCount` was not applied because the Compose Foundation API resolved by this project does not expose that `HorizontalPager` parameter. The dependency was not upgraded just for this optimization.

## Verification (Phase 4)

- Unit tests: `:app:testDebugUnitTest` passed after measurement infrastructure changes.
- Debug compile: `:app:compileDebugKotlin` passed after runtime fixes.
- Debug assemble: `:app:assembleDebug` passed after runtime fixes.
- Release lint: `:app:lintRelease` passed; the previous `NoCollectCallFound` error is resolved.
- Release assemble: passed with R8 and packaged Baseline Profile.
- Baseline Profile generation: `:app:generateBaselineProfile` passed; source profile exists under `app/src/release/generated/baselineProfiles/`.
- Connected benchmark: 4 benchmark tests passed; the direct connected task reports the generator test as skipped because it does not enable `RuleType.BaselineProfile`. The dedicated generate task produced the profile successfully.
- Cold startup median with profile: `331.85 ms` (`382.91 ms` without profile).
- Scroll frame CPU P50/P90/P95/P99 with profile: `3.47 / 18.85 / 19.77 / 23.82 ms`.
- Baseline Profile packaged: yes; source file should be committed.
- CI stability gate: no.
- Audit status: **targeted runtime fixes compiled and linted; new Macrobenchmark data intentionally not collected**.

## Recommended next run

1. Add a navigation-specific Macrobenchmark when performance numbers are requested.
2. Consider a Compose Foundation upgrade separately if adjacent-page precomposition is still required.
3. Add `ReportDrawnWhen` or `ReportDrawnAfter` before using time-to-full-display as the startup KPI.
4. Commit the generated profile and add a CI stability gate after the runtime baseline is accepted.
