package app.enzofeur.shopee.patches.shopee.performance

import app.morphe.patcher.Fingerprint

private const val CRONET_CONFIGURATION_CLASS = "Lcom/shopee/cronet/config/CronetConfiguration;"
private const val CRONET_ABTEST_CONFIG_CLASS = "Lcom/shopee/cronet/config/ABTestConfig;"
private const val HTTPDNS_HELPER_CLASS = "Lcom/shopee/app/network/httpdns/DnsHelper;"
private const val HOMEPAGE_TOGGLE_MANAGER_CLASS =
    "Lcom/shopee/app/biz/home/ui/home/native_home/experiment/HomepageFeatureToggleManager;"
private const val MAPPING_RULES_CLASS =
    "Lcom/shopee/app/biz/home/ui/home/native_home/MappingRules;"
private const val GLIDE_EXTENSION_KT_CLASS = "Lcom/shopee/core/imageloader/glide/ExtensionKt;"
private const val IMAGE_REQUEST_TYPE = "Lcom/shopee/core/imageloader/Request;"

internal val CRONET_BOOST_TRUE_FINGERPRINTS = listOf(
    "isZstdSupportEnabled",
    "isEnabledPrewarmUrl",
    "isMultipleAddressEnabled",
    "isEnabledCreateAlternativeConnect",
).map { methodName ->
    Fingerprint(
        definingClass = CRONET_CONFIGURATION_CLASS,
        name = methodName,
        returnType = "Z",
        parameters = listOf(),
    )
} + listOf(
    "getEnabledZstdSupport",
    "getPrewarmUrl",
).map { methodName ->
    Fingerprint(
        definingClass = CRONET_ABTEST_CONFIG_CLASS,
        name = methodName,
        returnType = "Z",
        parameters = listOf(),
    )
}

internal val CRONET_DNS_TELEMETRY_VOID_FINGERPRINTS = listOf(
    Fingerprint(
        definingClass = "Lcom/shopee/app/network/cronet/event/b;",
        name = "b",
        returnType = "V",
        parameters = listOf("Lorg/json/JSONObject;"),
    ),
    Fingerprint(
        definingClass = "Lcom/shopee/app/network/cronet/event/c;",
        name = "a",
        returnType = "V",
        parameters = listOf("Ljava/lang/String;"),
    ),
    Fingerprint(
        definingClass = "Lcom/shopee/app/network/cronet/f;",
        name = "onReport",
        returnType = "V",
        parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;"),
    ),
    Fingerprint(
        definingClass = HTTPDNS_HELPER_CLASS,
        name = "apmListener",
        returnType = "V",
        parameters = listOf(),
    ),
    Fingerprint(
        definingClass = HTTPDNS_HELPER_CLASS,
        name = "aptLogListener",
        returnType = "V",
        parameters = listOf(),
    ),
)

internal val FEED_PDP_TRUE_FINGERPRINTS = listOf(
    Fingerprint(
        definingClass = "Lcom/shopee/app/biz/launch/impl/ui/prefetch/PdpPrefetchConfig;",
        name = "a",
        returnType = "Z",
        parameters = listOf(),
    ),
) + listOf(
    "isThreadingOptimisationEnabled",
    "isPreloadMultiThreadDecode",
    "isBannerCacheOptimizationEnabled",
    "isHomepagePreloadImage",
    "isLazyTabarEnabled",
    "isLaunchKitStartupEnabled",
    "isImpressionAnrFixEnabled",
    "isRetreatCardAnimationOnLowEndDeviceEnabled",
    "isOptimisedLocationLogicEnabled",
    "isHomepageCacheKeyCompleteEnabled",
).map { methodName ->
    Fingerprint(
        definingClass = HOMEPAGE_TOGGLE_MANAGER_CLASS,
        name = methodName,
        returnType = "Z",
        parameters = listOf(),
    )
}

internal val FEED_OVERHEAD_FALSE_FINGERPRINTS = listOf(
    "isAptLogEnabled",
    "isHoldOutAptLogEnabled",
    "isHomeReplayLogEnabled",
    "getLandingBannerRealTimeTrackingEnabled",
).map { methodName ->
    Fingerprint(
        definingClass = HOMEPAGE_TOGGLE_MANAGER_CLASS,
        name = methodName,
        returnType = "Z",
        parameters = listOf(),
    )
} + listOf(
    Fingerprint(
        definingClass = MAPPING_RULES_CLASS,
        name = "supportGIF",
        returnType = "Z",
        parameters = listOf(),
    ),
)

internal val IMAGE_TELEMETRY_VOID_FINGERPRINTS = listOf(
    "notifyPerformanceOnRequestStartEventListeners",
    "notifyPerformanceOnDownloadStartedEventListeners",
    "notifyImagePerformanceOnDownloadSuccessEventListeners",
    "notifyImagePerformanceOnDownloadFailedEventListeners",
    "notifyPerformanceOnRequestEndEventListeners",
).map { methodName ->
    Fingerprint(
        definingClass = GLIDE_EXTENSION_KT_CLASS,
        name = methodName,
        returnType = "V",
        parameters = listOf(
            IMAGE_REQUEST_TYPE,
            "Ljava/util/List;",
            "Landroid/widget/ImageView;",
        ),
    )
}

internal val ImageSdnsMetricsUploadFingerprint = Fingerprint(
    definingClass = "Lcom/shopee/core/imageloader/ImageLoaderFeatureToggles;",
    name = "getEnableMmsSdkSdnsAbtestMetricsUpload",
    returnType = "Z",
    parameters = listOf(),
)

internal val ImageCrossFadeDurationFingerprint = Fingerprint(
    definingClass = IMAGE_REQUEST_TYPE,
    name = "getCrossFadeDurationMs",
    returnType = "Ljava/lang/Long;",
    parameters = listOf(),
)

private const val PRE_CREATE_RN_CONFIG_CLASS =
    "Lcom/shopee/app/container/rn/react/perf/RNContainerOptHelper\$PreCreateRNContextConfig;"

internal val PRE_CREATE_RN_TRUE_FINGERPRINTS = listOf(
    "getEnablePreCreateContext",
    "allScene",
    "getEnableCreateContextInAsyncThread",
    "getEnablePreLoadBaseBundle",
    "getEnableRemoveHermesDebugExecute",
).map { methodName ->
    Fingerprint(
        definingClass = PRE_CREATE_RN_CONFIG_CLASS,
        name = methodName,
        returnType = "Z",
        parameters = listOf(),
    )
}

internal val JsPreloadDelayEnabledInvokeFingerprint = Fingerprint(
    definingClass = "Lcom/shopee/app/container/rn/perf/h;",
    name = "invoke",
    returnType = "Ljava/lang/Object;",
    parameters = listOf(),
)
