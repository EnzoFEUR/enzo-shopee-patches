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
)

internal val PROTECTED_COMPONENT_PREFIXES = listOf(
    "com.google.firebase.messaging.",
    "androidx.work.",
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

/**
 * Sets `android:enabled="false"` on direct `<application>` child components matching third-party ad
 * or telemetry SDK namespaces. Returns the number of modified components.
 */
fun disableAdAndTelemetryComponents(manifest: ResourceXml): Int {
    val application = requireApplication(manifest)
    var disabledCount = 0

    for (component in application.children) {
        if (component.tag !in COMPONENT_TAGS) continue
        if (!isAdOrTelemetryComponent(component.attribute("android:name"))) continue

        component.setAttribute("android:enabled", "false")
        disabledCount++
    }

    return disabledCount
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
    val application = requireApplication(manifest)
    val existing = application.children("meta-data")
        .firstOrNull { it.attribute("android:name") == SMOKE_TEST_METADATA_NAME }

    if (existing != null) {
        existing.setAttribute("android:value", value)
        return
    }

    application.appendChild("meta-data") {
        setAttribute("android:name", SMOKE_TEST_METADATA_NAME)
        setAttribute("android:value", value)
    }
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
