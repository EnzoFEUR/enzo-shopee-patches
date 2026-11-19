package app.enzofeur.shopee.patches.shopee.ads

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.enzofeur.shopee.patches.shopee.manifest.disableAdAndTelemetryComponents
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceXml

@Suppress("unused")
val disableAdAndTelemetryComponentsPatch = patch(
    name = "Disable ad and telemetry SDK components",
    description = "Disables third-party ad, attribution, and telemetry services, receivers, providers, and activities in AndroidManifest.xml to reduce startup overhead and background wakeups.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    execute {
        resourceXml("AndroidManifest.xml").use { disableAdAndTelemetryComponents(it) }
    }
}
