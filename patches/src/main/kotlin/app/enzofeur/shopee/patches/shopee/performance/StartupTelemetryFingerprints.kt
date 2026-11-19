package app.enzofeur.shopee.patches.shopee.performance

import app.morphe.patcher.Fingerprint

private const val LUBAN_SDK_CLASS = "Lcom/shopee/luban/LuBanSDK;"

internal val ApmConfigInitValveRunFingerprint = Fingerprint(
    definingClass = "Lcom/shopee/app/application/launch/business/infra/ApmConfigInitValve;",
    name = "run",
    returnType = "V",
    parameters = listOf(),
)

internal val FirebaseAnalyticManagerSetupValveRunFingerprint = Fingerprint(
    definingClass = "Lcom/shopee/app/application/launch/business/infra/FirebaseAnalyticManagerSetupValve;",
    name = "run",
    returnType = "V",
    parameters = listOf(),
)

internal val LoggingInitPipelineValveRunFingerprint = Fingerprint(
    definingClass = "Lcom/shopee/app/application/launch/business/infra/LoggingInitPipelineValve;",
    name = "run",
    returnType = "V",
    parameters = listOf(),
)

internal val LuBanStartAfterLaunchFingerprint = Fingerprint(
    definingClass = LUBAN_SDK_CLASS,
    name = "startAfterLaunch",
    returnType = "V",
    parameters = listOf(),
)

internal val LuBanStartMainMonitorFingerprint = Fingerprint(
    definingClass = LUBAN_SDK_CLASS,
    name = "startMainMonitor",
    returnType = "V",
    parameters = listOf("Landroid/app/Application;"),
)

internal val LuBanStartFastStackSamplingFingerprint = Fingerprint(
    definingClass = LUBAN_SDK_CLASS,
    name = "startFastStackSampling",
    returnType = "V",
)

internal val LuBanStartStackLensFingerprint = Fingerprint(
    definingClass = LUBAN_SDK_CLASS,
    name = "startStackLens",
    returnType = "V",
)

internal val LuBanPreloadLubanFingerprint = Fingerprint(
    definingClass = LUBAN_SDK_CLASS,
    name = "preloadLuban",
    returnType = "V",
)

internal val LuBanPreloadAsyncFingerprint = Fingerprint(
    definingClass = LUBAN_SDK_CLASS,
    name = "preloadInsInAttachBaseContextAsync",
    returnType = "V",
)

internal val STARTUP_TELEMETRY_VALVE_FINGERPRINTS = listOf(
    ApmConfigInitValveRunFingerprint,
    FirebaseAnalyticManagerSetupValveRunFingerprint,
    LoggingInitPipelineValveRunFingerprint,
    LuBanStartAfterLaunchFingerprint,
    LuBanStartMainMonitorFingerprint,
    LuBanStartFastStackSamplingFingerprint,
    LuBanStartStackLensFingerprint,
    LuBanPreloadLubanFingerprint,
    LuBanPreloadAsyncFingerprint,
)
