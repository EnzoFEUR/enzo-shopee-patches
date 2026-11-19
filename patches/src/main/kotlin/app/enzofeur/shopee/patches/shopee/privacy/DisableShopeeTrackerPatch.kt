package app.enzofeur.shopee.patches.shopee.privacy

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.patch
import app.morphe.patcher.util.returnEarly

@Suppress("unused")
val disableShopeeTrackerPatch = patch(
    name = "Disable ShopeeTracker event collection",
    description = "Short-circuits ShopeeTracker event logging, MMAP buffer usage, scheduled uploads, and background flush calls while keeping ShopeeTrackerContentProvider intact for SESSION_ID reads.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    execute {
        var matched = 0

        SHOPEE_TRACKER_FALSE_FINGERPRINTS.forEach { fingerprint ->
            fingerprint.methodOrNull?.let {
                it.returnEarly(false)
                matched++
            }
        }

        SHOPEE_TRACKER_VOID_FINGERPRINTS.forEach { fingerprint ->
            fingerprint.methodOrNull?.let {
                it.returnEarly()
                matched++
            }
        }

        if (matched == 0) {
            throw PatchException("No ShopeeTracker target methods matched.")
        }
    }
}
