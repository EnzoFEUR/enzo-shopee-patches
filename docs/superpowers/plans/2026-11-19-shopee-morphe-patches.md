# Enzo Shopee Morphe Patches (`enzo-shopee-patches`) Implementation Plan

**Goal:** Set up a local, GitHub-ready Morphe Patcher 2.4.0 repository at `C:\Users\Enzo\.gemini\antigravity\scratch\enzo-shopee-patches` (cloned from `MorpheApp/morphe-patches-template`, with `origin` removed) containing four defensive Phase 1 manifest/resource optimization patches for `com.shopee.ph` (`3.85.28` + experimental latest), pure-Kotlin unit tests, updated docs/issue templates, and spec/plan records.

**Architecture:** Based directly on `MorpheApp/morphe-patches-template` (`dev` and `main` branches, `app.morphe.patches` Gradle plugin `2.1.1`, `morphe-patcher` `2.4.0`, untouched `.github/workflows/release.yml` and `.releaserc`). All manifest and ABI selection logic lives in pure helper functions inside `ManifestOptimization.kt` operating on Morphe's `ResourceXml` and `List<String>` so it can be unit-tested via `readXml(...)` without an APK, while each Morphe patch is a thin declarative wrapper in its own file under `patches/src/main/kotlin/app/enzofeur/shopee/patches/shopee/`.

**Tech Stack:** Kotlin (JVM / JDK 21), Gradle wrapper (`gradlew.bat`), `app.morphe.patches` 2.1.1, `app.morphe:morphe-patcher` 2.4.0, JUnit 4 (`junit:junit:4.13.2`), Git (local only).

## Summary of Steps

1. **Bootstrap Local Git Repo:** Clone `MorpheApp/morphe-patches-template` with `main` and `dev`, remove `origin`, and check out `dev`.
2. **Rebrand Configuration & Hygiene:** Update `settings.gradle.kts`, `gradle/libs.versions.toml`, `patches/build.gradle.kts`, `extensions/extension/build.gradle.kts`, `.gitignore`, and `.github/ISSUE_TEMPLATE/*`; replace template example Java/Kotlin files with `ShopeeExtension.java`; commit (`chore: configure template for Enzo Shopee Patches`).
3. **Compatibility & Pure Helpers:** Add `Constants.kt` (`COMPATIBILITY_SHOPEE_PH`) and `ManifestOptimization.kt` (`disableAdAndTelemetryComponents`, `disableStartupWakeReceivers`, `injectSmokeTestMetadata`, `selectUnusedAbiEntries`, `isAdOrTelemetryComponent`).
4. **Phase 1 Morphe Patches:** Add `DisableAdAndTelemetryComponentsPatch.kt`, `DisableStartupWakeReceiversPatch.kt`, `StripUnusedAbiLibrariesPatch.kt`, and `SmokeTestPatch.kt`.
5. **Unit Tests:** Add `ManifestOptimizationTest.kt` covering all 5 review-focus edge cases (relative/null component names, missing `<application>`, single-ABI protection, idempotent smoke-test metadata, protected FCM/WorkManager/hybrid receivers); commit (`feat: add Phase 1 Shopee manifest and ABI optimization patches`).
6. **Documentation & Records:** Update `README.md` (preserving `PATCHES_START` / `PATCHES_END`), save spec and implementation plan under `docs/superpowers/`, commit (`docs: add README, design spec, and implementation plan`).
7. **Verification:** Verify clean git state (`dev`/`main`, empty `git remote -v`), zero leftover template placeholders, and run `.\gradlew.bat :patches:test :patches:buildAndroid --no-daemon` to capture build/test output or environment blockers honestly.
