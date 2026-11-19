# Shopee Morphe Patches (`enzo-shopee-patches`) Design

Date: 2026-11-19
Status: Approved (Phase 1 implemented)

## Intent & Goal

Build a clean, Morphe-compliant Kotlin patch repository (`EnzoFEUR/enzo-shopee-patches`) that reduces Shopee Android third-party ad/telemetry overhead, startup wakeups, and multi-ABI footprint, while validating compatibility early against Shopee's `F13` integrity check before attempting deeper native bytecode hooks.

## Approaches Considered

1. **Morphe Patches Repo — Smoke-Test First + Native/Resource Optimization (Selected)**
   - Scaffold from `MorpheApp/morphe-patches-template` (`dev`/`main` branches, Gradle `.mpp` build, Morphe `release.yml`), start with a manifest-only smoke test and defensive resource/manifest patches, and defer bytecode fingerprints until an actual APK is supplied locally.
   - Tradeoff: Delivers a real Morphe `.mpp` project with minimal wasted work if a re-signed APK triggers `F13`, while non-root re-signing still carries `F13` risk and React Native OTA JS ads remain out of Dalvik/XML reach.
2. **Hybrid: Morphe Repo + Network Ad/Telemetry Blocklist**
   - Combines Approach 1 with a curated AdGuard/NextDNS/RethinkDNS domain blocklist. Deferred so Phase 1 stays focused on APK patching; can be layered later.
3. **Zero-Risk Fast Shopee (Stock APK + DNS/ADB Tuning)**
   - Avoids re-signing completely, but does not produce a Morphe `.mpp` patch bundle.

## Target APK & Repository Coordinates

- **Local repository path:** `C:\Users\Enzo\.gemini\antigravity\scratch\enzo-shopee-patches` (kept strictly local; no `git push` or remote creation).
- **Planned GitHub coordinates:** `EnzoFEUR/enzo-shopee-patches`
- **Gradle group / extension namespace:** `app.enzofeur.shopee` / `app.enzofeur.shopee.extension`
- **Target app:** `com.shopee.ph` (`Shopee PH`, `ApkFileType.APKM`, `appIconColor = 0xEE4D2D`), pinned to stable `3.85.28` (`arm64-v8a` from APKMirror) plus an experimental `version = null` target.
- **Why not Shopee Lite:** Shopee Lite is tailored to Indonesia (`com.shopee.id` / local payments) and is unsuitable for PH accounts.
- **APK hygiene:** `apks/`, `*.apk`, `*.apkm`, `*.apks`, `*.xapk` are gitignored so APKs are never committed.

## Phase 1 Patches

All manifest/ABI decision logic is isolated in pure helper functions inside `ManifestOptimization.kt` and tested with JUnit 4 via Morphe's `readXml(...)`:

1. **`Disable ad and telemetry SDK components`** (`default = true`)
   - Uses `resourceXml("AndroidManifest.xml")` to set `android:enabled="false"` on direct `<application>` child components (`service`, `receiver`, `provider`, `activity`, `activity-alias`) matching known third-party ad, attribution, and analytics namespaces (`gms.ads`, `gms.measurement`, `firebase.analytics`, `firebase.crashlytics`, `firebase.perf`, `appsflyer`, `adjust.sdk`, `facebook.appevents`, `facebook.ads`, `bytedance.sdk.openadsdk`, `pangle`, `applovin`, `ironsource`, `unity3d.ads`, `vungle`, `io.branch`).
   - Ignores missing or relative (`.`-prefixed) names and explicitly protects `com.google.firebase.messaging.*` and `androidx.work.*`.
2. **`Disable background boot and prefetch receivers`** (`default = false`)
   - Disables `<receiver>` components triggered by `BOOT_COMPLETED`, `LOCKED_BOOT_COMPLETED`, `QUICKBOOT_POWERON`, or `MY_PACKAGE_REPLACED` while preserving `com.google.firebase.messaging.*`, `androidx.work.*`, and receivers declaring `com.google.android.c2dm.intent.RECEIVE`.
3. **`Strip unused CPU architectures`** (`default = false`)
   - Uses `listApkEntries("lib/")` and `deleteResource(...)` with a `keepAbi` option (`arm64-v8a` default, or `armeabi-v7a`) to remove unused ABI native libraries from universal APKs, returning an empty list if the selected ABI is absent.
4. **`Patch smoke test`** (`default = false`)
   - Idempotently inserts/updates `<meta-data android:name="app.enzofeur.shopee.patched" android:value="true" />` so the user can test whether a re-signed Shopee APK boots and logs in without `F13`.

## Phase 2 Plan

Once the user provides `com.shopee.ph` `.apkm` locally in `apks/`, inspect DEX classes via `jadx` to extract verified fingerprints for native splash ads, promo dialogs, and video/live preloading.

## Non-Goals & Boundaries

- No signature verification, `F13`, Play Integrity, anti-tamper, or ShopeePay/checkout security bypasses.
- No fabricated Shopee bytecode fingerprints without inspecting a real APK.
- No manual edits to `.github/workflows/release.yml`, `.releaserc`, `patches-list.json`, `patches-bundle.json`, or `CHANGELOG.md`.
- Keep `gradle.properties` tracked and free of secrets.

## Local Build Prerequisites

- JDK 21 (present on machine).
- Android SDK (`ANDROID_HOME`) — must be installed locally for `buildAndroid`.
- GitHub Packages `read:packages` token (`gh auth refresh -s read:packages`) — needed to resolve `app.morphe.patches` from `maven.pkg.github.com/MorpheApp/registry`.
