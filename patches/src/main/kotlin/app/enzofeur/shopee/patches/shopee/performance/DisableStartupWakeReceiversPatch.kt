package app.enzofeur.shopee.patches.shopee.performance

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.enzofeur.shopee.patches.shopee.manifest.disableStartupWakeReceivers
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceXml

@Suppress("unused")
val disableStartupWakeReceiversPatch = patch(
    name = "Disable background boot and prefetch receivers",
    description = "Disables manifest receivers triggered on device boot or package replacement so Shopee stays dormant until launched.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    execute {
        resourceXml("AndroidManifest.xml").use { disableStartupWakeReceivers(it) }
    }
}
