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
    name = "Hide Shopee Videos",
    description = "Hides Shopee Video homepage shelves, video refresh jobs, video bottom-bar red dots, and standalone Video bottom tabs while keeping Shopee Live visible by default.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    extendWith("extensions/extension.mpe")

    val keepShopeeLive by booleanOption(
        key = "keepShopeeLive",
        default = true,
        title = "Keep Shopee Live visible",
        description = "Preserves Shopee Live homepage shelves, autoplay/previews, and Live/Live+Video bottom tabs while hiding Shopee Video.",
    )

    val hideHomeLiveAndVideo by booleanOption(
        key = "hideHomeLiveAndVideo",
        default = true,
        title = "Hide homepage Shopee Video sections",
        description = "Omits Video / combined Live+Video shelves on the home feed and disables video refresh/ad-refresh background jobs.",
    )

    val hideBottomVideoTab by booleanOption(
        key = "hideBottomVideoTab",
        default = true,
        title = "Hide Shopee Video bottom navigation tab",
        description = "Filters Video tabs out of the bottom bar and disables the video red-dot prompt.",
    )

    execute {
        val keepLive = keepShopeeLive == true
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

            if (!keepLive) {
                HOME_LIVE_FALSE_FINGERPRINTS.forEach { fingerprint ->
                    fingerprint.methodOrNull?.let {
                        it.returnEarly(false)
                        matched++
                    }
                }

                HOME_LIVE_VOID_FINGERPRINTS.forEach { fingerprint ->
                    fingerprint.methodOrNull?.let {
                        it.returnEarly()
                        matched++
                    }
                }

                MappingRulesEnableShopeeLiveFingerprint.methodOrNull?.let {
                    it.returnEarly("disable_live_streaming_1")
                    matched++
                }
            }
        }

        if (hideTab) {
            ToggleRedDotVideoBottomBarFingerprint.methodOrNull?.let {
                it.returnEarly(false)
                matched++
            }

            if (!keepLive) {
                BottomTabStartAnimationFingerprint.methodOrNull?.let {
                    it.returnEarly()
                    matched++
                }
            }

            val tabFilterHelper = if (keepLive) "filterVideoOnlyTabs" else "filterVideoAndLiveTabs"
            BottomTabGetTabViewDataFingerprint.methodOrNull?.let { method ->
                method.findInstructionIndicesReversedOrThrow(opcode(Opcode.RETURN_OBJECT)).forEach { index ->
                    val register = method.getInstruction<OneRegisterInstruction>(index).registerA
                    method.addInstructionsAtControlFlowLabel(
                        index,
                        """
                            invoke-static { v$register }, $EXTENSION_CLASS->$tabFilterHelper(Ljava/util/List;)Ljava/util/List;
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
