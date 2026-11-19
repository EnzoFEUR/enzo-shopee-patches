package app.enzofeur.shopee.patches.shopee.ads

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.enzofeur.shopee.patches.shopee.manifest.deactivateTelemetryMetadataAndRegistrars
import app.enzofeur.shopee.patches.shopee.manifest.disableAdAndTelemetryComponents
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceXml

@Suppress("unused")
val disableAdAndTelemetryComponentsPatch = patch(
    name = "Disable ad and telemetry SDK components",
    description = "Disables third-party and first-party telemetry/APM components, Firebase Analytics/Crashlytics/DataTransport registrars, and telemetry manifest flags in AndroidManifest.xml.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    execute {
        resourceXml("AndroidManifest.xml").use { manifest ->
            disableAdAndTelemetryComponents(manifest)
            deactivateTelemetryMetadataAndRegistrars(manifest)
        }
    }
}
