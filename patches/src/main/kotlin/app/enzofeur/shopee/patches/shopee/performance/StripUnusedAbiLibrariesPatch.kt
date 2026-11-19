package app.enzofeur.shopee.patches.shopee.performance

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.enzofeur.shopee.patches.shopee.manifest.selectUnusedAbiEntries
import app.morphe.patcher.patch.deleteResource
import app.morphe.patcher.patch.listApkEntries
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.stringOption

@Suppress("unused")
val stripUnusedAbiLibrariesPatch = patch(
    name = "Strip unused CPU architectures",
    description = "Removes native libraries for unused CPU architectures from universal APKs to reduce app size and startup scan overhead.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    val keepAbi by stringOption(
        key = "keepAbi",
        default = "arm64-v8a",
        values = mapOf(
            "ARM64 (Modern phones)" to "arm64-v8a",
            "ARMv7 (32-bit)" to "armeabi-v7a",
        ),
        title = "CPU architecture to keep",
        description = "Native library ABI directory to keep. No libraries are deleted if this ABI is absent.",
    )

    execute {
        val targetAbi = keepAbi ?: "arm64-v8a"
        val unused = selectUnusedAbiEntries(listApkEntries("lib/"), targetAbi)
        unused.forEach(::deleteResource)
    }
}
