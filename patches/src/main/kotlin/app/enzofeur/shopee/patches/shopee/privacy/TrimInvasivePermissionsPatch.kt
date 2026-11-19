package app.enzofeur.shopee.patches.shopee.privacy

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.enzofeur.shopee.patches.shopee.manifest.trimInvasivePermissions
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceXml

@Suppress("unused")
val trimInvasivePermissionsPatch = patch(
    name = "Trim invasive and battery-draining permissions",
    description = "Removes non-essential privacy, advertising, and background wake permissions (background location, AD_ID, call log, contacts, calendar, exact alarms, boot completed, OEM badges) while keeping camera, foreground location, notifications, and biometric auth intact.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    execute {
        resourceXml("AndroidManifest.xml").use { trimInvasivePermissions(it) }
    }
}
