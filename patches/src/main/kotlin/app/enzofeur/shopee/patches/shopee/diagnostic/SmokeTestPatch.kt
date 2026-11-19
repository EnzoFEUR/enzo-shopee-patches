package app.enzofeur.shopee.patches.shopee.diagnostic

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.enzofeur.shopee.patches.shopee.manifest.injectSmokeTestMetadata
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceXml

@Suppress("unused")
val smokeTestPatch = patch(
    name = "Patch smoke test",
    description = "Adds a harmless manifest metadata marker so you can test whether a re-signed Shopee APK boots and logs in without triggering F13 integrity errors.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    execute {
        resourceXml("AndroidManifest.xml").use { injectSmokeTestMetadata(it) }
    }
}
