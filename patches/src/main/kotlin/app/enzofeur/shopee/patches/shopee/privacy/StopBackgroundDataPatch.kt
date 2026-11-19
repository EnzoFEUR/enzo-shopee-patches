package app.enzofeur.shopee.patches.shopee.privacy

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.patch
import app.morphe.patcher.util.returnEarly

@Suppress("unused")
val stopBackgroundDataPatch = patch(
    name = "Stop background telemetry and sync workers",
    description = "Short-circuits background WorkManager telemetry uploaders so Shopee stays dormant in the background without triggering WorkManager retry loops.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    val disableAptLogReupload by booleanOption(
        key = "disableAptLogReupload",
        default = true,
        title = "Disable background AptLog re-upload worker",
        description = "Completes AptLogReuploadWorker immediately with Result.success() without waking the network.",
    )

    val disableBackgroundDataPointSync by booleanOption(
        key = "disableBackgroundDataPointSync",
        default = false,
        title = "Disable background DataPoint sync worker (Experimental)",
        description = "Skips background HFSyncInfo/LFSyncInfo/MFSyncInfo/PermissionStatusSyncInfo reporting inside DataPointService. Kept off by default to avoid potential F13 risk on sensitive accounts.",
    )

    val disableBackgroundRnBundleSync by booleanOption(
        key = "disableBackgroundRnBundleSync",
        default = false,
        title = "Disable background React Native OTA bundle sync",
        description = "Stops BundleSyncService from downloading React Native OTA updates in the background while the app is closed.",
    )

    execute {
        val stopAptLog = disableAptLogReupload == true
        val stopDataPoint = disableBackgroundDataPointSync == true
        val stopBundleSync = disableBackgroundRnBundleSync == true
        var matched = 0

        if (stopAptLog) {
            AptLogReuploadDoWorkFingerprint.methodOrNull?.let { method ->
                method.addInstructions(
                    0,
                    """
                        new-instance v0, Landroidx/work/s${'$'}a${'$'}c;
                        invoke-direct { v0 }, Landroidx/work/s${'$'}a${'$'}c;-><init>()V
                        return-object v0
                    """,
                )
                matched++
            }
        }

        if (stopDataPoint) {
            DataPointTriggerRunFingerprint.methodOrNull?.let {
                it.returnEarly()
                matched++
            }
        }

        if (stopBundleSync) {
            BundleSyncDoInBackgroundFingerprint.methodOrNull?.let { method ->
                method.addInstructions(
                    0,
                    """
                        const/4 v0, 0x0
                        return-object v0
                    """,
                )
                matched++
            }
        }

        if ((stopAptLog || stopDataPoint || stopBundleSync) && matched == 0) {
            throw PatchException("No background worker target methods matched.")
        }
    }
}
