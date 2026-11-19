package app.enzofeur.shopee.patches.shopee.live

import app.morphe.patcher.Fingerprint

private const val MMC_PLAYER_NATIVE_CLASS = "Lcom/mmc/player/MMCPlayerNative;"
private const val MMC_AB_TEST_CLASS = "Lcom/mmc/player/utils/MMCSPABTestUtilsV2;"
private const val MMC_HARDWARE_DECODER_CLASS = "Lcom/mmc/player/decoder/MMCHardwareDecoder;"
private const val LIVETECH_REPORTER_CLASS =
    "Lcom/shopee/livetechsdk/trackreport/SZLiveTechTrackingReporter;"
private const val SZ_TRACKING_KIT_CLASS = "Lcom/shopee/live/livewrapper/sztrackingkit/a;"
private const val LIVE_INFO_ENTITY_TYPE = "Lcom/shopee/livetechsdk/trackreport/LiveInfoEntity;"
private const val ABSTRACT_EVENT_CREATOR_TYPE =
    "Lcom/shopee/livetechsdk/trackreport/creator/AbstractSZTrackingEventCreator;"

internal val CallNativeSetDataSourceWithConfigFingerprint = Fingerprint(
    definingClass = MMC_PLAYER_NATIVE_CLASS,
    name = "callNativeSetDataSourceWithConfig",
    returnType = "V",
    parameters = listOf(
        "J",
        "Ljava/lang/String;",
        "Z",
        "[Ljava/lang/String;",
        "[Ljava/lang/Object;",
    ),
)

internal val CallNativeSetDatasourceFingerprint = Fingerprint(
    definingClass = MMC_PLAYER_NATIVE_CLASS,
    name = "callNativeSetDatasource",
    returnType = "I",
    parameters = listOf("J", "Ljava/lang/String;", "Z", "Z"),
)

internal val CallNativeUpdateLasUrlFingerprint = Fingerprint(
    definingClass = MMC_PLAYER_NATIVE_CLASS,
    name = "callNativeUpdateLasUrl",
    returnType = "V",
    parameters = listOf("J", "Ljava/lang/String;"),
)

internal val CallNativeSetPlayerConfigFingerprint = Fingerprint(
    definingClass = MMC_PLAYER_NATIVE_CLASS,
    name = "callNativeSetPlayerConfig",
    returnType = "V",
    parameters = listOf("J", "Lcom/mmc/player/MMCPlayerConfig;"),
)

internal val MmsTransportP2pPercentageFingerprint = Fingerprint(
    definingClass =
        "Lcom/shopee/szconfigurationcenter/network/model/MMSTransportSdkUserPercentageModel;",
    name = "getP2pPercentageValue",
    returnType = "I",
    parameters = listOf(),
)

internal val SzHardwareCodecEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/shopee/szconfigurationcenter/b;",
    name = "f",
    returnType = "Z",
    parameters = listOf(),
)

private fun abTestTrue(vararg names: String) = names.map { methodName ->
    Fingerprint(
        definingClass = MMC_AB_TEST_CLASS,
        name = methodName,
        returnType = "Z",
        parameters = listOf(),
    )
}

// Safe: faster connect/startup, no buffer or decoder behavior change.
internal val LIVE_ABTEST_SAFE_FINGERPRINTS = abTestTrue(
    "enableDnsIpv4Only",
    "enableLivestreamPreCreateDecoder",
)

// Aggressive: smaller buffers / low-delay decode; can stutter on jittery networks or some SoCs.
internal val LIVE_ABTEST_AGGRESSIVE_FINGERPRINTS = abTestTrue(
    "getMediaCodecGetFrameOpt",
    "enableFDKAAC",
    "useDummyViewOpt",
    "getBanSwitchUrlOnLowestLevel",
)

internal val HardwareDecoderEnableLowDelayFingerprint = Fingerprint(
    definingClass = MMC_HARDWARE_DECODER_CLASS,
    name = "enableLowDelayMode",
    returnType = "V",
    parameters = listOf("Z"),
)

internal val HardwareDecoderCreateMediaCodecFingerprint = Fingerprint(
    definingClass = MMC_HARDWARE_DECODER_CLASS,
    name = "createMediaCodec",
    returnType = "V",
    parameters = listOf(),
)

internal val HardwareDecoderCreateMediaCodecWithListenerFingerprint = Fingerprint(
    definingClass = MMC_HARDWARE_DECODER_CLASS,
    name = "createMediaCodec",
    returnType = "V",
    parameters = listOf(
        "Landroid/media/MediaCodec\$OnFrameRenderedListener;",
        "Landroid/os/Handler;",
    ),
)

internal val LiveSnapshotConfigEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/shopee/video_player/snapshot/h;",
    name = "a",
    returnType = "Z",
    parameters = listOf(),
)

internal val LIVE_TELEMETRY_VOID_FINGERPRINTS = listOf(
    Fingerprint(
        definingClass = "Lcom/shopee/livetechtrackreport/b;",
        name = "b",
        returnType = "V",
        parameters = listOf("Lcom/squareup/wire/Message;"),
    ),
    Fingerprint(
        definingClass = "Lcom/shopee/livetechtrackreport/b;",
        name = "c",
        returnType = "V",
        parameters = listOf("Ljava/util/List;"),
    ),
    Fingerprint(
        definingClass = LIVETECH_REPORTER_CLASS,
        name = "setCanReport",
        returnType = "V",
        parameters = listOf("Z"),
    ),
    Fingerprint(
        definingClass = LIVETECH_REPORTER_CLASS,
        name = "handleEventReportWithCheckParams",
        returnType = "V",
        parameters = listOf(ABSTRACT_EVENT_CREATOR_TYPE, LIVE_INFO_ENTITY_TYPE),
    ),
    Fingerprint(
        definingClass = LIVETECH_REPORTER_CLASS,
        name = "handleEventReportWithNotCheckParams",
        returnType = "V",
        parameters = listOf(ABSTRACT_EVENT_CREATOR_TYPE, LIVE_INFO_ENTITY_TYPE),
    ),
    Fingerprint(
        definingClass = LIVETECH_REPORTER_CLASS,
        name = "handleReplayEventReportWithCheckParams",
        returnType = "V",
        parameters = listOf(ABSTRACT_EVENT_CREATOR_TYPE, LIVE_INFO_ENTITY_TYPE),
    ),
    Fingerprint(
        definingClass = LIVETECH_REPORTER_CLASS,
        name = "onNetStatus",
        returnType = "V",
        parameters = listOf("Landroid/os/Bundle;", LIVE_INFO_ENTITY_TYPE),
    ),
    Fingerprint(
        definingClass = LIVETECH_REPORTER_CLASS,
        name = "onVodNetStatus",
        returnType = "V",
        parameters = listOf(LIVE_INFO_ENTITY_TYPE, "Landroid/os/Bundle;"),
    ),
    Fingerprint(
        definingClass = LIVETECH_REPORTER_CLASS,
        name = "loopCallback",
        returnType = "V",
        parameters = listOf("I", LIVE_INFO_ENTITY_TYPE),
    ),
    Fingerprint(
        definingClass = SZ_TRACKING_KIT_CLASS,
        name = "c",
        returnType = "V",
        parameters = listOf("Lcom/squareup/wire/Message;"),
    ),
    Fingerprint(
        definingClass = SZ_TRACKING_KIT_CLASS,
        name = "d",
        returnType = "V",
        parameters = listOf(
            "Ljava/lang/String;",
            "Ljava/lang/String;",
            "Z",
            "I",
            "J",
            "Z",
        ),
    ),
    Fingerprint(
        definingClass = SZ_TRACKING_KIT_CLASS,
        name = "e",
        returnType = "V",
        parameters = listOf("I", "Ljava/lang/String;"),
    ),
    Fingerprint(
        definingClass = "Lcom/mmc/player/utils/RepeatReportTask;",
        name = "start",
        returnType = "V",
        parameters = listOf("J"),
    ),
)
