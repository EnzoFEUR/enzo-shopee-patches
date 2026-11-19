# 🛍️🧩 Enzo Shopee Patches

Performance and ad/telemetry reduction [Morphe](https://morphe.software) patches for Shopee Android (`com.shopee.ph`).

## ❓ About

Shopee Lite-style optimization patches for `com.shopee.ph` (`3.85.28`), combining manifest telemetry/registrar cleanup, invasive permission trimming, verified bytecode short-circuits for `ShopeeTracker`, cold-start `LuBanSDK`/APM valves, Live & Video auto-preload control, low-latency Live Selling tuning with stream resolution selection, Cronet/item/image loading optimization, and background WorkManager suppression.

> [!NOTE]
> **Sign-in & `F13` notes:** Shopee enforces APK signature and integrity checks (`F13`). Re-signed APKs cannot use native Google Sign-In, Facebook SSO, Passkeys, or Google SMS Retriever auto-fill because those bind to Shopee's official signing certificate — use **Phone/Email + Password + WhatsApp OTP** to sign in cleanly. This repository intentionally does **not** include signature, anti-tamper, Play Integrity, or ShopeePay/checkout security bypasses.

### How to use these patches

Once published to GitHub, click the button below or manually add this repository URL to Morphe Manager: `https://github.com/EnzoFEUR/enzo-shopee-patches`

<a href="https://morphe.software/add-source?github=EnzoFEUR/enzo-shopee-patches">
  <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe" height="48">
</a>

## 📦 Best Target APK

- **Recommended package & version:** `com.shopee.ph` **v3.85.28** (latest stable, non-beta **`arm64-v8a`** `.apkm` split bundle from APKMirror).
- **Watching Shopee Live Selling?** Shopee ships Live Selling (`livestreaming`) as a Play Dynamic Feature Module that is missing from APKMirror `.apkm` bundles and cannot download on re-signed builds. To watch Live Selling in a patched APK: install official Shopee from Google Play, open **Live** once so Play downloads the `livestreaming` split, extract the complete installed split bundle via **AntiSplit-M**, and patch that bundle (keeping **Hide Shopee Video and Live** disabled if you want Live entrypoints visible).
- **Why not Shopee Lite?** Shopee Lite is tailored primarily to the Indonesia market (`com.shopee.id` / local payment integrations) and is incompatible with PH/other regional accounts and promos.
- **Never commit APKs:** Place local test bundles under `apks/` (gitignored along with `*.apk`, `*.apkm`, `*.apks`, and `*.xapk`).

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.4](https://github.com/EnzoFEUR/enzo-shopee-patches/releases/tag/v1.0.0-dev.4)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;12 patches total
<details open>
<summary>📦 Shopee PH&nbsp;&nbsp;•&nbsp;&nbsp;12 patches</summary>
<br>

**Package name:** `com.shopee.ph`

**🎯 Supported versions:**

| 3.85.28 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Boost Shopee Live latency and quality](#boost-shopee-live-latency-and-quality) | Applies smooth fast-connect live optimizations (IPv4-only DNS, hardware decoding, decoder pre-creation, smooth URL switching, zero P2P uploads), adds a stream resolution preset picker (Auto/Lowest/Fast/Balanced/Highest), and stops live telemetry heartbeats and hidden screenshot uploads. | • Live stream resolution preset<br>• Lock selected resolution<br>• Enable fast-connect network boost<br>• Aggressive low-delay mode (Experimental)<br>• Disable Live P2P peer uploading<br>• Disable Live telemetry & snapshot uploads<br>• Instant live promo price refresh |
| [Disable Live and Video auto-preloading](#disable-live-and-video-auto-preloading) | Disables background Shopee Video download, floating Live video, and Check-In widget components and skips eager Live/Video startup preloading and auto-landing prefetchers. |  |
| [Disable ShopeeTracker event collection](#disable-shopeetracker-event-collection) | Short-circuits ShopeeTracker event logging, MMAP buffer usage, scheduled uploads, and background flush calls while keeping ShopeeTrackerContentProvider intact for SESSION_ID reads. |  |
| [Disable ad and telemetry SDK components](#disable-ad-and-telemetry-sdk-components) | Disables third-party and first-party telemetry/APM components, Firebase Analytics/Crashlytics/DataTransport registrars, and telemetry manifest flags in AndroidManifest.xml. |  |
| [Disable background boot and prefetch receivers](#disable-background-boot-and-prefetch-receivers) | Disables manifest receivers triggered on device boot or package replacement so Shopee stays dormant until launched. |  |
| [Disable cold-start APM and profiler valves](#disable-cold-start-apm-and-profiler-valves) | Skips non-critical cold-start APM, Firebase Analytics, logging pipeline valves, and Luban/StackLens background sampling to cut startup latency, CPU usage, and battery drain. |  |
| [Hide Shopee Videos](#hide-shopee-videos) | Hides Shopee Video homepage shelves, video refresh jobs, video bottom-bar red dots, and standalone Video bottom tabs while keeping Shopee Live visible by default. | • Keep Shopee Live visible<br>• Hide homepage Shopee Video sections<br>• Hide Shopee Video bottom navigation tab |
| [Optimize item and network loading](#optimize-item-and-network-loading) | Accelerates product, PDP, checkout, and feed loading by enabling Cronet Zstd/QUIC prewarming and multi-IP failover, native PDP prefetching, background React Native context/bundle prewarming, multi-threaded feed decode, instant image rendering, and removing per-request network and image telemetry overhead. | • Enable Cronet Zstd, prewarm, and telemetry removal<br>• Enable native PDP & feed performance toggles<br>• Prewarm Checkout & React Native container<br>• Instant product image rendering |
| [Patch smoke test](#patch-smoke-test) | Adds a harmless manifest metadata marker so you can test whether a re-signed Shopee APK boots and logs in without triggering F13 integrity errors. |  |
| [Stop background telemetry and sync workers](#stop-background-telemetry-and-sync-workers) | Short-circuits background WorkManager telemetry uploaders so Shopee stays dormant in the background without triggering WorkManager retry loops. | • Disable background AptLog re-upload worker<br>• Disable background DataPoint sync worker (Experimental)<br>• Disable background React Native OTA bundle sync |
| [Strip unused CPU architectures](#strip-unused-cpu-architectures) | Removes native libraries for unused CPU architectures from universal APKs to reduce app size and startup scan overhead. | • CPU architecture to keep |
| [Trim invasive and battery-draining permissions](#trim-invasive-and-battery-draining-permissions) | Removes non-essential privacy, advertising, and background wake permissions (background location, AD_ID, call log, contacts, calendar, exact alarms, boot completed, OEM badges) while keeping camera, foreground location, notifications, and biometric auth intact. |  |

</details>

<!-- PATCHES_END -->

## 🛠️ Building locally

### Prerequisites

1. **JDK 21** (Temurin / Eclipse Adoptium 21 LTS).
2. **Android SDK** installed with `ANDROID_HOME` set (including `build-tools` and an Android platform).
3. **GitHub Packages `read:packages` authentication** (required by `settings.gradle.kts` to resolve `app.morphe.patches` from `https://maven.pkg.github.com/MorpheApp/registry`):
   ```powershell
   gh auth refresh -s read:packages
   $env:GITHUB_ACTOR = "EnzoFEUR"
   $env:GITHUB_TOKEN = (gh auth token)
   ```
   Or configure `gpr.user` and `gpr.key` in `~/.gradle/gradle.properties` (never commit tokens to the repository's `gradle.properties`).

### Build & test

```powershell
.\gradlew.bat :patches:test :patches:buildAndroid
```

The built patch bundle `.mpp` is output to `patches/build/libs/patches-*.mpp` and can be applied locally via [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop).

## 🚀 Publishing to GitHub

1. Create the repository `EnzoFEUR/enzo-shopee-patches` on GitHub.
2. In **Settings → Actions → General → Workflow permissions**, enable **Allow GitHub Actions to create and approve pull requests**.
3. Push both `main` and `dev` branches:
   ```powershell
   git remote add origin https://github.com/EnzoFEUR/enzo-shopee-patches.git
   git push -u origin main dev
   ```
4. Use semantic commits on `dev` (`feat:`, `fix:`, `perf:`, `chore:`); `.github/workflows/release.yml` handles building `.mpp` pre-releases on `dev` and stable releases when `dev` is merged into `main`.

## 🗺️ Roadmap

- **Implemented:** Deep manifest telemetry/registrar deactivation, `ShopeeTracker` kill-switch, cold-start APM/`LuBanSDK`/logging valves, Live & Video auto-preload control, background boot receiver suppressor, multi-ABI stripping, opt-in permission trimmer, opt-in Video/Live UI hiding, Live low-latency network & decoder boost with LAS resolution preset picker, Cronet/PDP/image loading optimization, and background WorkManager uploader suppression (verified against `com.shopee.ph` `3.85.28`).

## 📜 License

Enzo Shopee Patches are licensed under the [GNU General Public License v3.0](LICENSE).
