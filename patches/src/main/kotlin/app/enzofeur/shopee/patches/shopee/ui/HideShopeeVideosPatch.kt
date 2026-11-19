package app.enzofeur.shopee.patches.shopee.ui

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.patch
import app.morphe.patcher.util.addInstructionsAtControlFlowLabel
import app.morphe.patcher.util.findInstructionIndicesReversedOrThrow
import app.morphe.patcher.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS = "Lapp/enzofeur/shopee/extension/ShopeeExtension;"

@Suppress("unused")
val hideShopeeVideosPatch = patch(
    name = "Hide Shopee Video and Live",
    description = "Removes the Shopee Live & Video homepage section before view inflation, disables homepage video autoplay/refresh jobs, and optionally hides the Live & Video bottom navigation tab.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    extendWith("extensions/extension.mpe")

    val hideHomeLiveAndVideo by booleanOption(
        key = "hideHomeLiveAndVideo",
        default = true,
        title = "Hide homepage Shopee Live & Video cards",
        description = "Omits the Shopee Live & Video section on the home feed and disables autoplay and background ad-refresh jobs.",
    )

    val hideBottomVideoTab by booleanOption(
        key = "hideBottomVideoTab",
        default = true,
        title = "Hide Live & Video bottom navigation tab",
        description = "Filters the Live & Video tab out of the bottom bar and disables red-dot/icon animations.",
    )

    execute {
        val hideHome = hideHomeLiveAndVideo == true
        val hideTab = hideBottomVideoTab == true
        var matched = 0

        if (hideHome) {
            HOME_VIDEO_FALSE_FINGERPRINTS.forEach { fingerprint ->
                fingerprint.methodOrNull?.let {
                    it.returnEarly(false)
                    matched++
                }
            }

            HOME_VIDEO_VOID_FINGERPRINTS.forEach { fingerprint ->
                fingerprint.methodOrNull?.let {
                    it.returnEarly()
                    matched++
                }
            }

            MappingRulesEnableLiveAndVideoFingerprint.methodOrNull?.let {
                it.returnEarly("disable_live_and_video")
                matched++
            }

            MappingRulesEnableShopeeLiveFingerprint.methodOrNull?.let {
                it.returnEarly("disable_live_streaming_1")
                matched++
            }
        }

        if (hideTab) {
            ToggleRedDotVideoBottomBarFingerprint.methodOrNull?.let {
                it.returnEarly(false)
                matched++
            }

            BottomTabStartAnimationFingerprint.methodOrNull?.let {
                it.returnEarly()
                matched++
            }

            BottomTabGetTabViewDataFingerprint.methodOrNull?.let { method ->
                method.findInstructionIndicesReversedOrThrow(opcode(Opcode.RETURN_OBJECT)).forEach { index ->
                    val register = method.getInstruction<OneRegisterInstruction>(index).registerA
                    method.addInstructionsAtControlFlowLabel(
                        index,
                        """
                            invoke-static { v$register }, $EXTENSION_CLASS->filterVideoAndLiveTabs(Ljava/util/List;)Ljava/util/List;
                            move-result-object v$register
                        """,
                    )
                }
                matched++
            }
        }

        if ((hideHome || hideTab) && matched == 0) {
            throw PatchException("No Shopee Video/Live UI target methods matched.")
        }
    }
}
