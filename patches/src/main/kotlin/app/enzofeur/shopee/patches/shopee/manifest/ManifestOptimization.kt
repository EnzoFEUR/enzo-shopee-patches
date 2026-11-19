package app.enzofeur.shopee.patches.shopee.manifest

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.xml.ResourceXml
import app.morphe.patcher.xml.XmlElement

const val SMOKE_TEST_METADATA_NAME = "app.enzofeur.shopee.patched"

internal val COMPONENT_TAGS = setOf(
    "service",
    "receiver",
    "provider",
    "activity",
    "activity-alias",
)

internal val AD_AND_TELEMETRY_COMPONENT_PREFIXES = listOf(
    "com.google.android.gms.ads.",
    "com.google.android.gms.measurement.",
    "com.google.android.gms.tagmanager.",
    "com.google.android.datatransport.runtime.",
    "com.google.firebase.analytics.",
    "com.google.firebase.crashlytics.",
    "com.google.firebase.perf.",
    "com.appsflyer.",
    "com.adjust.sdk.",
    "com.facebook.appevents.",
    "com.facebook.ads.",
    "com.bytedance.sdk.openadsdk.",
    "com.pangle.",
    "com.applovin.",
    "com.ironsource.",
    "com.unity3d.ads.",
    "com.vungle.",
    "io.branch.",
    "com.shopee.luban.module.focuswindow.",
    "com.shopee.app.infra.tracker.util.ShopeeInstallReceiver",
    "com.shopee.app.application.tracking.TrackerDebugControlReceiver",
    "com.shopee.app.apm.autotest.",
    "com.huawei.hms.mlsdk.",
    "com.huawei.agconnect.",
)

internal val PROTECTED_COMPONENT_PREFIXES = listOf(
    "com.google.firebase.messaging.",
    "androidx.work.",
    "com.shopee.shopeetracker.",
    "com.shopee.shpssdk",
    "com.shopee.shopeepaysdk.",
)

internal val TELEMETRY_METADATA_OVERRIDES: Map<String, String> = linkedMapOf(
    "google_analytics_deferred_deep_link_enabled" to "false",
    "firebase_analytics_collection_deactivated" to "true",
    "firebase_analytics_collection_enabled" to "false",
    "firebase_crashlytics_collection_enabled" to "false",
    "firebase_performance_collection_enabled" to "false",
    "google_analytics_adid_collection_enabled" to "false",
    "google_analytics_ssaid_collection_enabled" to "false",
    "com.facebook.sdk.AutoLogAppEventsEnabled" to "false",
    "com.facebook.sdk.AdvertiserIDCollectionEnabled" to "false",
)

internal val REMOVABLE_FIREBASE_REGISTRARS = setOf(
    "com.google.firebase.components:com.google.firebase.analytics.connector.internal.AnalyticsConnectorRegistrar",
    "com.google.firebase.components:com.google.firebase.crashlytics.CrashlyticsRegistrar",
    "com.google.firebase.components:com.google.firebase.datatransport.TransportRegistrar",
)

internal val VIDEO_AND_LIVE_BACKGROUND_COMPONENTS = setOf(
    "com.shopee.sz.luckyvideo.common.utils.download.VideoDownloadService",
    "com.shopee.live.livestreaming.audience.floatwindow.base.service.FloatVideoService",
    "com.shopee.live.livestreaming.util.NetworkStateReceiver",
    "com.shopee.addon.widgetcheckin.CheckInAppWidget",
    "com.shopee.addon.widgetcheckin.receiver.UpdateWidgetReceiver",
)

internal val INVASIVE_PERMISSIONS = setOf(
    "android.permission.ACCESS_BACKGROUND_LOCATION",
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE",
    "android.permission.READ_CALL_LOG",
    "android.permission.READ_CONTACTS",
    "android.permission.READ_CALENDAR",
    "android.permission.WRITE_CALENDAR",
    "android.permission.SCHEDULE_EXACT_ALARM",
    "android.permission.RECEIVE_BOOT_COMPLETED",
    "com.huawei.appmarket.service.commondata.permission.GET_COMMON_DATA",
    "com.sec.android.provider.badge.permission.READ",
    "com.sec.android.provider.badge.permission.WRITE",
    "com.htc.launcher.permission.READ_SETTINGS",
    "com.htc.launcher.permission.UPDATE_SHORTCUT",
    "com.sonyericsson.home.permission.BROADCAST_BADGE",
    "com.sonymobile.home.permission.PROVIDER_INSERT_BADGE",
    "com.anddoes.launcher.permission.UPDATE_COUNT",
    "com.majeur.launcher.permission.UPDATE_BADGE",
    "com.huawei.android.launcher.permission.CHANGE_BADGE",
    "com.huawei.android.launcher.permission.READ_SETTINGS",
    "com.huawei.android.launcher.permission.WRITE_SETTINGS",
)

internal val WAKE_RECEIVER_ACTIONS = setOf(
    "android.intent.action.BOOT_COMPLETED",
    "android.intent.action.LOCKED_BOOT_COMPLETED",
    "android.intent.action.QUICKBOOT_POWERON",
    "com.htc.intent.action.QUICKBOOT_POWERON",
    "android.intent.action.MY_PACKAGE_REPLACED",
)

internal val PROTECTED_RECEIVER_ACTIONS = setOf(
    "com.google.android.c2dm.intent.RECEIVE",
)

private fun requireApplication(manifest: ResourceXml): XmlElement =
    manifest.root.children("application").firstOrNull()
        ?: throw PatchException("AndroidManifest.xml is missing <application> element.")

/**
 * Returns true if [componentName] belongs to a known third-party ad, attribution, or telemetry SDK
 * and is not in a protected push or background-work namespace.
 */
fun isAdOrTelemetryComponent(componentName: String?): Boolean {
    if (componentName.isNullOrBlank() || componentName.startsWith(".")) return false
    if (PROTECTED_COMPONENT_PREFIXES.any { componentName.startsWith(it) }) return false
    return AD_AND_TELEMETRY_COMPONENT_PREFIXES.any { componentName.startsWith(it) }
}

private fun disableMatchingComponents(
    manifest: ResourceXml,
    predicate: (String?) -> Boolean,
): Int {
    val application = requireApplication(manifest)
    var disabledCount = 0

    for (component in application.children) {
        if (component.tag !in COMPONENT_TAGS) continue
        if (!predicate(component.attribute("android:name"))) continue

        component.setAttribute("android:enabled", "false")
        disabledCount++
    }

    return disabledCount
}

private fun upsertMetaData(parent: XmlElement, name: String, value: String) {
    val existing = parent.children("meta-data")
        .firstOrNull { it.attribute("android:name") == name }

    if (existing != null) {
        existing.setAttribute("android:value", value)
        return
    }

    parent.appendChild("meta-data") {
        setAttribute("android:name", name)
        setAttribute("android:value", value)
    }
}

/**
 * Sets `android:enabled="false"` on direct `<application>` child components matching third-party ad
 * or telemetry SDK namespaces. Returns the number of modified components.
 */
fun disableAdAndTelemetryComponents(manifest: ResourceXml): Int =
    disableMatchingComponents(manifest, ::isAdOrTelemetryComponent)

/**
 * Disables Firebase/Google/Meta telemetry flags via `<meta-data>` overrides and removes non-essential
 * Firebase analytics, crashlytics, and datatransport registrars from `ComponentDiscoveryService`.
 * Returns the number of upserted or removed `<meta-data>` entries.
 */
fun deactivateTelemetryMetadataAndRegistrars(manifest: ResourceXml): Int {
    val application = requireApplication(manifest)
    var updatedCount = 0

    for ((key, value) in TELEMETRY_METADATA_OVERRIDES) {
        upsertMetaData(application, key, value)
        updatedCount++
    }

    val discoveryService = application.children("service")
        .firstOrNull { it.attribute("android:name") == "com.google.firebase.components.ComponentDiscoveryService" }
    if (discoveryService != null) {
        for (meta in discoveryService.children("meta-data").toList()) {
            if (meta.attribute("android:name") !in REMOVABLE_FIREBASE_REGISTRARS) continue
            meta.remove()
            updatedCount++
        }
    }

    return updatedCount
}

/**
 * Sets `android:enabled="false"` on background Shopee Video, Live floating-window, and Check-In
 * widget components in `AndroidManifest.xml`. Returns the number of disabled components.
 */
fun disableVideoAndLiveBackgroundComponents(manifest: ResourceXml): Int =
    disableMatchingComponents(manifest) { it in VIDEO_AND_LIVE_BACKGROUND_COMPONENTS }

/**
 * Removes non-essential privacy, advertising, and background wake `<uses-permission>` entries from
 * `<manifest>`. Returns the number of removed permission entries.
 */
fun trimInvasivePermissions(manifest: ResourceXml): Int {
    var removedCount = 0

    for (permission in manifest.root.children("uses-permission").toList()) {
        if (permission.attribute("android:name") !in INVASIVE_PERMISSIONS) continue
        permission.remove()
        removedCount++
    }

    return removedCount
}

/**
 * Sets `android:enabled="false"` on manifest `<receiver>` elements listening for boot or package
 * replacement actions, while preserving protected push/work components and FCM receivers.
 */
fun disableStartupWakeReceivers(manifest: ResourceXml): Int {
    val application = requireApplication(manifest)
    var disabledCount = 0

    for (receiver in application.children("receiver")) {
        val name = receiver.attribute("android:name")
        if (name.isNullOrBlank()) continue
        if (PROTECTED_COMPONENT_PREFIXES.any { name.startsWith(it) }) continue

        val actions = receiver.elements("action")
            .mapNotNull { it.attribute("android:name") }
            .toSet()

        val hasWakeAction = actions.any { it in WAKE_RECEIVER_ACTIONS }
        val hasProtectedAction = actions.any { it in PROTECTED_RECEIVER_ACTIONS }
        if (!hasWakeAction || hasProtectedAction) continue

        receiver.setAttribute("android:enabled", "false")
        disabledCount++
    }

    return disabledCount
}

/**
 * Inserts or updates the harmless `<meta-data>` smoke-test marker inside `<application>`.
 */
fun injectSmokeTestMetadata(manifest: ResourceXml, value: String = "true") {
    upsertMetaData(requireApplication(manifest), SMOKE_TEST_METADATA_NAME, value)
}

/**
 * Returns native library APK entries outside `lib/<keepAbi>/`. Returns an empty list if [keepAbi]
 * is absent from [entries] so single-ABI APKs are never stripped empty.
 */
fun selectUnusedAbiEntries(entries: List<String>, keepAbi: String = "arm64-v8a"): List<String> {
    val libEntries = entries.filter { it.startsWith("lib/") }
    val keepPrefix = "lib/$keepAbi/"
    if (libEntries.none { it.startsWith(keepPrefix) }) return emptyList()
    return libEntries.filterNot { it.startsWith(keepPrefix) }
}
