package app.enzofeur.shopee.patches.shopee.live

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.util.returnEarly
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.iface.instruction.Instruction

private const val EXTENSION_CLASS = "Lapp/enzofeur/shopee/extension/ShopeeExtension;"
private const val MMC_PLAYER_CONFIG_CLASS = "Lcom/mmc/player/MMCPlayerConfig;"
private const val MMC_HARDWARE_DECODER_CLASS = "Lcom/mmc/player/decoder/MMCHardwareDecoder;"

private val VALID_RESOLUTION_PRESETS = setOf("lowest", "fast", "balanced", "highest", "auto")

@Suppress("unused")
val boostShopeeLivePatch = patch(
    name = "Boost Shopee Live latency and quality",
    description = "Applies smooth fast-connect live optimizations (IPv4-only DNS, hardware decoding, decoder pre-creation, smooth URL switching, zero P2P uploads), adds a stream resolution preset picker (Auto/Lowest/Fast/Balanced/Highest), and stops live telemetry heartbeats and hidden screenshot uploads.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    extendWith("extensions/extension.mpe")

    val liveResolution by stringOption(
        key = "liveResolution",
        default = "auto",
        values = mapOf(
            "Auto (Shopee ABR, smoothest)" to "auto",
            "Lowest (Fastest / Minimal Delay)" to "lowest",
            "Fast (480p / 540p)" to "fast",
            "Balanced (720p)" to "balanced",
            "Highest (1080p / Best Quality)" to "highest",
        ),
        title = "Live stream resolution preset",
        description = "Starting resolution tier for Shopee Live. Auto keeps adaptive bitrate (recommended on mobile data or weak Wi-Fi). Other presets start at that tier.",
        required = true,
    )

    val lockSelectedResolution by booleanOption(
        key = "lockSelectedResolution",
        default = false,
        title = "Lock selected resolution",
        description = "Strips all other tiers so the player can never switch. Removes ABR fallback, so it stutters if your network dips below the tier's bitrate (ignored when Auto is selected).",
    )

    val lowLatencyNetworkBoost by booleanOption(
        key = "lowLatencyNetworkBoost",
        default = true,
        title = "Enable fast-connect network boost",
        description = "IPv4-only live DNS, connection reuse, smooth URL switching, and decoder pre-creation for faster stream start. Does not shrink buffers.",
    )

    val aggressiveLowDelay by booleanOption(
        key = "aggressiveLowDelay",
        default = false,
        title = "Aggressive low-delay mode (Experimental)",
        description = "Smaller playback buffers, MediaCodec low-delay decoding, FDK-AAC, and frame-get optimizations. Lowers delay on strong, stable Wi-Fi but can cause stutter on mobile data or some devices.",
    )

    val disableLiveP2p by booleanOption(
        key = "disableLiveP2p",
        default = true,
        title = "Disable Live P2P peer uploading",
        description = "Prevents WebRTC/STUN peer-to-peer stream sharing during Live sessions to avoid upload bandwidth drain and peer stall latency.",
    )

    val disableLiveTelemetryAndSnapshots by booleanOption(
        key = "disableLiveTelemetryAndSnapshots",
        default = true,
        title = "Disable Live telemetry & snapshot uploads",
        description = "Disables continuous live QoS/heartbeat reporters, CPU/thermal polling threads, and hidden playback screenshot sampling/uploads.",
    )

    execute {
        val preset = (liveResolution ?: "auto").trim().lowercase()
        if (preset !in VALID_RESOLUTION_PRESETS) {
            throw PatchException("Unsupported Shopee Live resolution preset: $liveResolution")
        }

        val lock = lockSelectedResolution == true
        val boost = lowLatencyNetworkBoost == true
        val aggressive = aggressiveLowDelay == true
        val noP2p = disableLiveP2p == true
        val noTelemetry = disableLiveTelemetryAndSnapshots == true
        val presetConfig = "$preset:${if (lock) 1 else 0}:${if (noP2p) 1 else 0}"
        var matched = 0

        if (preset != "auto" || noP2p) {
            listOf(
                CallNativeSetDataSourceWithConfigFingerprint,
                CallNativeSetDatasourceFingerprint,
            ).forEach { fingerprint ->
                fingerprint.methodOrNull?.let { method ->
                    method.addInstructions(
                        0,
                        """
                            const-string v0, "$presetConfig"
                            invoke-static { p3, p4, v0 }, $EXTENSION_CLASS->rewriteLiveStreamSource(Ljava/lang/String;ZLjava/lang/String;)Ljava/lang/String;
                            move-result-object p3
                        """,
                    )
                    matched++
                }
            }

            CallNativeUpdateLasUrlFingerprint.methodOrNull?.let { method ->
                method.addInstructions(
                    0,
                    """
                        const-string v0, "$presetConfig"
                        invoke-static { p3, v0 }, $EXTENSION_CLASS->rewriteLasManifest(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
                        move-result-object p3
                    """,
                )
                matched++
            }

            if (noP2p) {
                MmsTransportP2pPercentageFingerprint.methodOrNull?.let {
                    it.returnEarly(0)
                    matched++
                }
            }
        }

        if (boost) {
            LIVE_ABTEST_SAFE_FINGERPRINTS.forEach { fingerprint ->
                fingerprint.methodOrNull?.let {
                    it.returnEarly(true)
                    matched++
                }
            }

            SzHardwareCodecEnabledFingerprint.methodOrNull?.let {
                it.returnEarly(true)
                matched++
            }
        }

        if (aggressive) {
            LIVE_ABTEST_AGGRESSIVE_FINGERPRINTS.forEach { fingerprint ->
                fingerprint.methodOrNull?.let {
                    it.returnEarly(true)
                    matched++
                }
            }

            HardwareDecoderEnableLowDelayFingerprint.methodOrNull?.let { method ->
                method.addInstructions(0, "const/4 p1, 0x1")
                matched++
            }

            listOf(
                HardwareDecoderCreateMediaCodecFingerprint,
                HardwareDecoderCreateMediaCodecWithListenerFingerprint,
            ).forEach { fingerprint ->
                fingerprint.methodOrNull?.let { method ->
                    method.addInstructions(
                        0,
                        """
                            const/4 v0, 0x1
                            iput-boolean v0, p0, $MMC_HARDWARE_DECODER_CLASS->mLowDelayMode:Z
                        """,
                    )
                    matched++
                }
            }
        }

        if (boost || aggressive || noTelemetry) {
            CallNativeSetPlayerConfigFingerprint.methodOrNull?.let { method ->
                val targetInstruction: Instruction = method.getInstruction(0)
                val smali = buildString {
                    appendLine("if-eqz p3, :skip_live_player_config")
                    if (boost || aggressive) appendLine("const/4 v0, 0x1")
                    if (boost) {
                        appendLine("iput-boolean v0, p3, $MMC_PLAYER_CONFIG_CLASS->enableDnsIpv4Only:Z")
                        appendLine("iput-boolean v0, p3, $MMC_PLAYER_CONFIG_CLASS->enableLivestreamPreCreateDecoder:Z")
                        appendLine("iput-boolean v0, p3, $MMC_PLAYER_CONFIG_CLASS->enableSwitchUrlSmoothly:Z")
                        appendLine("iput-boolean v0, p3, $MMC_PLAYER_CONFIG_CLASS->enableKeepPulling:Z")
                    }
                    if (aggressive) {
                        appendLine("iput-boolean v0, p3, $MMC_PLAYER_CONFIG_CLASS->enableBufferSizeOpt:Z")
                        appendLine("iput-boolean v0, p3, $MMC_PLAYER_CONFIG_CLASS->enableMediacodecGetFrameOpt:Z")
                        appendLine("iput-boolean v0, p3, $MMC_PLAYER_CONFIG_CLASS->enableFDKAAC:Z")
                        appendLine("iput-boolean v0, p3, $MMC_PLAYER_CONFIG_CLASS->useDummyViewOpt:Z")
                        appendLine("iput-boolean v0, p3, $MMC_PLAYER_CONFIG_CLASS->enableBanSwitchUrlOnLowestLevel:Z")
                    }
                    if (boost) {
                        appendLine("const/4 v0, 0x2")
                        appendLine("iput v0, p3, $MMC_PLAYER_CONFIG_CLASS->reuseLinkOpt:I")
                    }
                    if (noTelemetry) {
                        appendLine("const/4 v0, 0x0")
                        appendLine("invoke-virtual { p3, v0 }, $MMC_PLAYER_CONFIG_CLASS->setReportLevel(I)V")
                    }
                }
                method.addInstructionsWithLabels(
                    0,
                    smali,
                    ExternalLabel("skip_live_player_config", targetInstruction),
                )
                matched++
            }
        }

        if (noTelemetry) {
            LIVE_TELEMETRY_VOID_FINGERPRINTS.forEach { fingerprint ->
                fingerprint.methodOrNull?.let {
                    it.returnEarly()
                    matched++
                }
            }

            LiveSnapshotConfigEnabledFingerprint.methodOrNull?.let {
                it.returnEarly(false)
                matched++
            }
        }

        if ((preset != "auto" || boost || aggressive || noP2p || noTelemetry) && matched == 0) {
            throw PatchException("No Shopee Live latency/quality target methods matched.")
        }
    }
}
