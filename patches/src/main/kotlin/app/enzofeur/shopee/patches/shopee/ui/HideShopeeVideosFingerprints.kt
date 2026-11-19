package app.enzofeur.shopee.patches.shopee.ui

import app.morphe.patcher.Fingerprint

private const val TOGGLE_MANAGER_CLASS =
    "Lcom/shopee/app/biz/home/ui/home/native_home/experiment/HomepageFeatureToggleManager;"
private const val MAPPING_RULES_CLASS =
    "Lcom/shopee/app/biz/home/ui/home/native_home/MappingRules;"
private const val LIVE_AND_VIDEO_CELL_CLASS =
    "Lcom/shopee/app/biz/home/ui/home/native_home/cell/virtualview/ShopeeLiveAndVideoCell;"
private const val LIVE_AND_VIDEO_COMPANION_CLASS =
    "Lcom/shopee/app/biz/home/ui/home/native_home/cell/virtualview/ShopeeLiveAndVideoCell\$Companion;"
private const val LIVE_CELL_CLASS =
    "Lcom/shopee/app/biz/home/ui/home/native_home/cell/virtualview/ShopeeLiveCell;"
private const val BOTTOM_TAB_MANAGER_CLASS =
    "Lcom/shopee/app/biz/home/ui/home/native_home/dynamic/tabmanager/BottomTabManager;"
private const val BASE_CELL_TYPE = "Lcom/shopee/leego/structure/BaseCell;"

internal val ToggleLiveAndVideoEnabledFingerprint = Fingerprint(
    definingClass = TOGGLE_MANAGER_CLASS,
    name = "isLiveAndVideoEnabled",
    returnType = "Z",
    parameters = listOf(),
)

internal val ToggleLiveSectionEnabledFingerprint = Fingerprint(
    definingClass = TOGGLE_MANAGER_CLASS,
    name = "isLiveStreamingSectionEnabled",
    returnType = "Z",
    parameters = listOf(),
)

internal val ToggleAutoPlayOnFingerprint = Fingerprint(
    definingClass = TOGGLE_MANAGER_CLASS,
    name = "isAutoPlayOn",
    returnType = "Z",
    parameters = listOf(),
)

internal val ToggleLivestreamAutoplayFingerprint = Fingerprint(
    definingClass = TOGGLE_MANAGER_CLASS,
    name = "isLivestreamAutoplayEnabled",
    returnType = "Z",
    parameters = listOf(),
)

internal val ToggleLivestreamAutoplayAllFingerprint = Fingerprint(
    definingClass = TOGGLE_MANAGER_CLASS,
    name = "isLivestreamAutoplayAllEnabled",
    returnType = "Z",
    parameters = listOf(),
)

internal val ToggleHomepageRefreshVideoFingerprint = Fingerprint(
    definingClass = TOGGLE_MANAGER_CLASS,
    name = "isHomepageRefreshVideoEnable",
    returnType = "Z",
    parameters = listOf(),
)

internal val ToggleHomepageRefreshLiveAdsFingerprint = Fingerprint(
    definingClass = TOGGLE_MANAGER_CLASS,
    name = "isHomepageRefreshLiveAdsEnable",
    returnType = "Z",
    parameters = listOf(),
)

internal val LiveAndVideoCellIsValidFingerprint = Fingerprint(
    definingClass = LIVE_AND_VIDEO_CELL_CLASS,
    name = "isValid",
    returnType = "Z",
    parameters = listOf(BASE_CELL_TYPE),
)

internal val LiveAndVideoCellDispatchIsValidFingerprint = Fingerprint(
    definingClass = LIVE_AND_VIDEO_CELL_CLASS,
    name = "dispatchIsValid",
    returnType = "Z",
    parameters = listOf(BASE_CELL_TYPE),
)

internal val LiveCellIsValidFingerprint = Fingerprint(
    definingClass = LIVE_CELL_CLASS,
    name = "isValid",
    returnType = "Z",
    parameters = listOf(BASE_CELL_TYPE),
)

internal val LiveCellDispatchIsValidFingerprint = Fingerprint(
    definingClass = LIVE_CELL_CLASS,
    name = "dispatchIsValid",
    returnType = "Z",
    parameters = listOf(BASE_CELL_TYPE),
)

internal val HOME_VIDEO_FALSE_FINGERPRINTS = listOf(
    ToggleLiveAndVideoEnabledFingerprint,
    ToggleLiveSectionEnabledFingerprint,
    ToggleAutoPlayOnFingerprint,
    ToggleLivestreamAutoplayFingerprint,
    ToggleLivestreamAutoplayAllFingerprint,
    ToggleHomepageRefreshVideoFingerprint,
    ToggleHomepageRefreshLiveAdsFingerprint,
    LiveAndVideoCellIsValidFingerprint,
    LiveAndVideoCellDispatchIsValidFingerprint,
    LiveCellIsValidFingerprint,
    LiveCellDispatchIsValidFingerprint,
)

internal val LiveAndVideoCellRefreshFingerprint = Fingerprint(
    definingClass = LIVE_AND_VIDEO_CELL_CLASS,
    name = "refresh",
    returnType = "V",
    parameters = listOf(),
)

internal val LiveCellRefreshFingerprint = Fingerprint(
    definingClass = LIVE_CELL_CLASS,
    name = "refresh",
    returnType = "V",
    parameters = listOf(),
)

internal val LiveAdsOutsideJobFingerprint = Fingerprint(
    definingClass = LIVE_AND_VIDEO_COMPANION_CLASS,
    name = "startJobRemoveAllAdsOutsideHomepage",
    returnType = "V",
    parameters = listOf(),
)

internal val LiveInvalidAdsOutsideJobFingerprint = Fingerprint(
    definingClass = LIVE_AND_VIDEO_COMPANION_CLASS,
    name = "startJobRemoveAndRefreshInvalidAdsOutsideHomepage",
    returnType = "V",
    parameters = listOf(),
)

internal val LiveInvalidAdsWithinJobFingerprint = Fingerprint(
    definingClass = LIVE_AND_VIDEO_COMPANION_CLASS,
    name = "startJobRemoveAndRefreshInvalidAdsWithinHomepage",
    returnType = "V",
    parameters = listOf(),
)

internal val HOME_VIDEO_VOID_FINGERPRINTS = listOf(
    LiveAndVideoCellRefreshFingerprint,
    LiveCellRefreshFingerprint,
    LiveAdsOutsideJobFingerprint,
    LiveInvalidAdsOutsideJobFingerprint,
    LiveInvalidAdsWithinJobFingerprint,
)

internal val MappingRulesEnableLiveAndVideoFingerprint = Fingerprint(
    definingClass = MAPPING_RULES_CLASS,
    name = "enableLiveAndVideo",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Lorg/json/JSONObject;"),
)

internal val MappingRulesEnableShopeeLiveFingerprint = Fingerprint(
    definingClass = MAPPING_RULES_CLASS,
    name = "enableShopeeLive",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Lorg/json/JSONObject;", "Ljava/lang/Integer;", "Ljava/lang/Integer;"),
)

internal val BottomTabGetTabViewDataFingerprint = Fingerprint(
    definingClass = BOTTOM_TAB_MANAGER_CLASS,
    name = "getTabViewData",
    returnType = "Ljava/util/List;",
    parameters = listOf(),
)

internal val BottomTabStartAnimationFingerprint = Fingerprint(
    definingClass = BOTTOM_TAB_MANAGER_CLASS,
    name = "startAnimation",
    returnType = "V",
    parameters = listOf(),
)

internal val ToggleRedDotVideoBottomBarFingerprint = Fingerprint(
    definingClass = TOGGLE_MANAGER_CLASS,
    name = "isRedDotVideoBottomBarEnabled",
    returnType = "Z",
    parameters = listOf(),
)
