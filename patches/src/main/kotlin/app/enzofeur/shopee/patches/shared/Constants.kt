package app.enzofeur.shopee.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_SHOPEE_PH = Compatibility(
        name = "Shopee PH",
        packageName = "com.shopee.ph",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xEE4D2D,
        targets = listOf(
            AppTarget(
                version = "3.85.28",
            ),
            AppTarget(
                version = null,
                isExperimental = true,
            ),
        ),
    )
}
