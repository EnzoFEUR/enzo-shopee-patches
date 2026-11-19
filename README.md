# 🛍️🧩 Enzo Shopee Patches

Performance and ad/telemetry reduction [Morphe](https://morphe.software) patches for Shopee Android (`com.shopee.ph`).

## ❓ About

Phase 1 provides defensive, APK-level manifest and architecture optimization patches designed to lower startup overhead, strip third-party ad/attribution SDK components, suppress background boot wakeups, and validate device compatibility early via a harmless smoke test before adding deeper bytecode hooks.

> [!NOTE]
> **Integrity / `F13` smoke-test-first workflow:** Shopee enforces APK signature and app-integrity checks (which commonly flag re-signed builds with login/checkout error `F13`). Enable **Patch smoke test** on a clean base APK first to verify whether a re-signed build boots and logs in on your device before investing in additional patches. This repository intentionally does **not** include signature, anti-tamper, Play Integrity, or ShopeePay/checkout security bypasses.

### How to use these patches

Once published to GitHub, click the button below or manually add this repository URL to Morphe Manager: `https://github.com/EnzoFEUR/enzo-shopee-patches`

<a href="https://morphe.software/add-source?github=EnzoFEUR/enzo-shopee-patches">
  <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe" height="48">
</a>

## 📦 Best Target APK

- **Recommended package & version:** `com.shopee.ph` **v3.85.28** (latest stable, non-beta **`arm64-v8a`** `.apkm` split bundle from APKMirror).
- **Why not Shopee Lite?** Shopee Lite is tailored primarily to the Indonesia market (`com.shopee.id` / local payment integrations) and is incompatible with PH/other regional accounts and promos.
- **Never commit APKs:** Place local test bundles under `apks/` (gitignored along with `*.apk`, `*.apkm`, `*.apks`, and `*.xapk`).

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->

<!-- Do not modify this section by hand. The patch list is generated when release.yml creates a new release.
     
     If you wish for the patches list to be collapsed, then remove the word 'EXPANDED' from the comment tag above.

     If you wish to manually keep this list updated then remove the PATCHES_START and PATCHES_END 
     comment blocks entirely. -->

#### A list of your patches will automatically be shown here after your first patches release is created.

&nbsp;

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

## 🗺️ Phase 2 Roadmap

After confirming the Phase 1 smoke test on your target device:

1. Drop your pinned `com.shopee.ph` `.apkm` into `apks/`.
2. Inspect the DEX classes and React Native bridge locally with `jadx`.
3. Add verified bytecode fingerprints for native splash ads, promo dialogs, and video/live preloading hooks inside `patches/src/main/kotlin/app/enzofeur/shopee/patches/shopee/` and `extensions/extension/`.

## 📜 License

Enzo Shopee Patches are licensed under the [GNU General Public License v3.0](LICENSE).
