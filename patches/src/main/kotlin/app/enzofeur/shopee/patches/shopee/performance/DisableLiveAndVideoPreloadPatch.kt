package app.enzofeur.shopee.patches.shopee.performance

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.enzofeur.shopee.patches.shopee.manifest.disableVideoAndLiveBackgroundComponents
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceXml
import app.morphe.patcher.util.returnEarly

@Suppress("unused")
val disableLiveAndVideoPreloadPatch = patch(
    name = "Disable Live and Video auto-preloading",
    description = "Disables background Shopee Video download, floating Live video, and Check-In widget components and skips eager Live/Video startup preloading and auto-landing prefetchers.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    execute {
        resourceXml("AndroidManifest.xml").use { disableVideoAndLiveBackgroundComponents(it) }

        var matched = 0

        LIVE_VIDEO_PRELOAD_FINGERPRINTS.forEach { fingerprint ->
            fingerprint.methodOrNull?.let {
                it.returnEarly()
                matched++
            }
        }

        if (matched == 0) {
            throw PatchException("No Shopee Live/Video preload target methods matched.")
        }
    }
}
