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
> **[v1.0.0-dev.1](https://github.com/EnzoFEUR/enzo-shopee-patches/releases/tag/v1.0.0-dev.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
<details open>
<summary>📦 Shopee PH&nbsp;&nbsp;•&nbsp;&nbsp;4 patches</summary>
<br>

**Package name:** `com.shopee.ph`

**🎯 Supported versions:**

| 3.85.28 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable ad and telemetry SDK components](#disable-ad-and-telemetry-sdk-components) | Disables third-party ad, attribution, and telemetry services, receivers, providers, and activities in AndroidManifest.xml to reduce startup overhead and background wakeups. |  |
| [Disable background boot and prefetch receivers](#disable-background-boot-and-prefetch-receivers) | Disables manifest receivers triggered on device boot or package replacement so Shopee stays dormant until launched. |  |
| [Patch smoke test](#patch-smoke-test) | Adds a harmless manifest metadata marker so you can test whether a re-signed Shopee APK boots and logs in without triggering F13 integrity errors. |  |
| [Strip unused CPU architectures](#strip-unused-cpu-architectures) | Removes native libraries for unused CPU architectures from universal APKs to reduce app size and startup scan overhead. | • CPU architecture to keep |

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
