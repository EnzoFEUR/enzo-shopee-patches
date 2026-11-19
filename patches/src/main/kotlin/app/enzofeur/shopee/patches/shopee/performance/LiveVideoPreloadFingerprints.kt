package app.enzofeur.shopee.patches.shopee.performance

import app.morphe.patcher.Fingerprint

private const val LUCKY_VIDEO_INIT_PROVIDER_CLASS = "Lcom/shopee/sz/luckyvideo/LuckyVideoInitProvider;"

internal val LiveApplicationSetupRunSafelyFingerprint = Fingerprint(
    definingClass = "Lcom/shopee/app/application/launch/business/LiveApplicationSetup;",
    name = "runSafely",
    returnType = "V",
    parameters = listOf(),
)

internal val AutoLandingPreFetcherFingerprint = Fingerprint(
    definingClass = "Lcom/shopee/sz/luckyvideo/mixtab2/impl/view/player/video/AutoLandingPreFetcher;",
    name = "prefetchMixTabAutoLandingVideoData",
    returnType = "V",
    parameters = listOf("L"),
)

internal val LuckyVideoAfterLaunchFingerprint = Fingerprint(
    definingClass = LUCKY_VIDEO_INIT_PROVIDER_CLASS,
    name = "afterLaunch",
    returnType = "V",
    parameters = listOf("Landroid/app/Application;"),
)

internal val LuckyVideoRegisterBootCallbackFingerprint = Fingerprint(
    definingClass = LUCKY_VIDEO_INIT_PROVIDER_CLASS,
    name = "registerBootCallback",
    returnType = "V",
    parameters = listOf(),
)

internal val LIVE_VIDEO_PRELOAD_FINGERPRINTS = listOf(
    LiveApplicationSetupRunSafelyFingerprint,
    AutoLandingPreFetcherFingerprint,
    LuckyVideoAfterLaunchFingerprint,
    LuckyVideoRegisterBootCallbackFingerprint,
)
