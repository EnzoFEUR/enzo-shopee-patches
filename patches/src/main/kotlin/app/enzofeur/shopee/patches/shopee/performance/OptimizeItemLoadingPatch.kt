package app.enzofeur.shopee.patches.shopee.performance

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.patch
import app.morphe.patcher.util.returnEarly

@Suppress("unused")
val optimizeItemLoadingPatch = patch(
    name = "Optimize item and network loading",
    description = "Accelerates product, PDP, checkout, and feed loading by enabling Cronet Zstd/QUIC prewarming and multi-IP failover, native PDP prefetching, background React Native context/bundle prewarming, multi-threaded feed decode, instant image rendering, and removing per-request network and image telemetry overhead.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    val enableCronetNetworkBoost by booleanOption(
        key = "enableCronetNetworkBoost",
        default = true,
        title = "Enable Cronet Zstd, prewarm, and telemetry removal",
        description = "Forces Cronet Zstd compression, URL prewarming, alternative connection setup, multi-address failover, and strips per-request Cronet/HttpDNS telemetry reporters.",
    )

    val enableFeedAndPdpPrefetch by booleanOption(
        key = "enableFeedAndPdpPrefetch",
        default = true,
        title = "Enable native PDP & feed performance toggles",
        description = "Enables native product-detail prefetching, multi-threaded feed decode, image preloading, lazy tab bar, ANR fixes, and disables home GIF decoding and feed AptLog telemetry.",
    )

    val prewarmCheckoutAndRnContainer by booleanOption(
        key = "prewarmCheckoutAndRnContainer",
        default = true,
        title = "Prewarm Checkout & React Native container",
        description = "Pre-creates the React Native context asynchronously, preloads the base JS bundle, skips JS preload startup delays, and removes Hermes debug overhead so Checkout and Cart screens open faster.",
    )

    val instantProductImages by booleanOption(
        key = "instantProductImages",
        default = true,
        title = "Instant product image rendering",
        description = "Eliminates Glide image fade-in animation delays and skips per-image performance telemetry callbacks and SDNS metrics uploads.",
    )

    execute {
        val boostCronet = enableCronetNetworkBoost == true
        val boostFeed = enableFeedAndPdpPrefetch == true
        val prewarmRn = prewarmCheckoutAndRnContainer == true
        val boostImages = instantProductImages == true
        var matched = 0

        if (boostCronet) {
            CRONET_BOOST_TRUE_FINGERPRINTS.forEach { fingerprint ->
                fingerprint.methodOrNull?.let {
                    it.returnEarly(true)
                    matched++
                }
            }

            CRONET_DNS_TELEMETRY_VOID_FINGERPRINTS.forEach { fingerprint ->
                fingerprint.methodOrNull?.let {
                    it.returnEarly()
                    matched++
                }
            }
        }

        if (boostFeed) {
            FEED_PDP_TRUE_FINGERPRINTS.forEach { fingerprint ->
                fingerprint.methodOrNull?.let {
                    it.returnEarly(true)
                    matched++
                }
            }

            FEED_OVERHEAD_FALSE_FINGERPRINTS.forEach { fingerprint ->
                fingerprint.methodOrNull?.let {
                    it.returnEarly(false)
                    matched++
                }
            }
        }

        if (prewarmRn) {
            PRE_CREATE_RN_TRUE_FINGERPRINTS.forEach { fingerprint ->
                fingerprint.methodOrNull?.let {
                    it.returnEarly(true)
                    matched++
                }
            }

            JsPreloadDelayEnabledInvokeFingerprint.methodOrNull?.let { method ->
                method.addInstructions(
                    0,
                    """
                        sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                        return-object v0
                    """,
                )
                matched++
            }
        }

        if (boostImages) {
            IMAGE_TELEMETRY_VOID_FINGERPRINTS.forEach { fingerprint ->
                fingerprint.methodOrNull?.let {
                    it.returnEarly()
                    matched++
                }
            }

            ImageSdnsMetricsUploadFingerprint.methodOrNull?.let {
                it.returnEarly(false)
                matched++
            }

            ImageCrossFadeDurationFingerprint.methodOrNull?.let { method ->
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

        if ((boostCronet || boostFeed || prewarmRn || boostImages) && matched == 0) {
            throw PatchException("No Shopee item/network optimization target methods matched.")
        }
    }
}
