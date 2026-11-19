package app.enzofeur.shopee.patches.shopee.manifest

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.readXml
import app.morphe.patcher.xml.ResourceXml
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream

class ManifestOptimizationTest {
    private fun parseXml(content: String): ResourceXml =
        readXml(ByteArrayInputStream(content.trimIndent().toByteArray(Charsets.UTF_8)))

    @Test
    fun disablesThirdPartyAdAndTelemetryComponentsAndProtectsCoreAndRelativeNames() {
        val manifest = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.shopee.ph">
                <application android:name="com.shopee.app.ShopeeApplication">
                    <service android:name="com.google.android.gms.measurement.AppMeasurementService" />
                    <receiver android:name="com.appsflyer.SingleInstallBroadcastReceiver" />
                    <provider android:name="com.google.firebase.analytics.FirebaseInitProvider" />
                    <service android:name="com.google.firebase.messaging.FirebaseMessagingService" />
                    <provider android:name="androidx.work.impl.WorkManagerInitializer" />
                    <service android:name=".analytics.ShopeeInternalService" />
                    <activity android:name="com.shopee.app.ui.home.HomeActivity" />
                    <service />
                </application>
            </manifest>
        """

        parseXml(manifest).use { xml ->
            val count = disableAdAndTelemetryComponents(xml)
            assertEquals(3, count)

            val app = xml.root.children("application").single()
            assertEquals(
                "false",
                app.elementByAttribute("android:name", "com.google.android.gms.measurement.AppMeasurementService")
                    .attribute("android:enabled"),
            )
            assertEquals(
                "false",
                app.elementByAttribute("android:name", "com.appsflyer.SingleInstallBroadcastReceiver")
                    .attribute("android:enabled"),
            )
            assertEquals(
                "false",
                app.elementByAttribute("android:name", "com.google.firebase.analytics.FirebaseInitProvider")
                    .attribute("android:enabled"),
            )
            assertNull(
                app.elementByAttribute("android:name", "com.google.firebase.messaging.FirebaseMessagingService")
                    .attribute("android:enabled"),
            )
            assertNull(
                app.elementByAttribute("android:name", "androidx.work.impl.WorkManagerInitializer")
                    .attribute("android:enabled"),
            )
            assertNull(
                app.elementByAttribute("android:name", ".analytics.ShopeeInternalService")
                    .attribute("android:enabled"),
            )
            assertNull(
                app.elementByAttribute("android:name", "com.shopee.app.ui.home.HomeActivity")
                    .attribute("android:enabled"),
            )
            val unnamedService = app.children("service").last()
            assertNull(unnamedService.attribute("android:enabled"))
        }
    }

    @Test
    fun throwsPatchExceptionWhenApplicationElementIsMissing() {
        val manifest = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.shopee.ph" />
        """

        parseXml(manifest).use { xml ->
            assertThrows(PatchException::class.java) {
                disableAdAndTelemetryComponents(xml)
            }
            assertThrows(PatchException::class.java) {
                disableStartupWakeReceivers(xml)
            }
            assertThrows(PatchException::class.java) {
                injectSmokeTestMetadata(xml)
            }
        }
    }

    @Test
    fun disablesBootAndPackageReplacedReceiversWhileProtectingFcmAndWorkManager() {
        val manifest = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.shopee.ph">
                <application>
                    <receiver android:name="com.shopee.app.startup.BootReceiver">
                        <intent-filter>
                            <action android:name="android.intent.action.BOOT_COMPLETED" />
                        </intent-filter>
                    </receiver>
                    <receiver android:name="com.shopee.app.startup.UpgradeReceiver">
                        <intent-filter>
                            <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
                        </intent-filter>
                    </receiver>
                    <receiver android:name="com.google.firebase.messaging.FirebaseMessagingReceiver">
                        <intent-filter>
                            <action android:name="android.intent.action.BOOT_COMPLETED" />
                        </intent-filter>
                    </receiver>
                    <receiver android:name="com.shopee.app.push.HybridPushReceiver">
                        <intent-filter>
                            <action android:name="android.intent.action.BOOT_COMPLETED" />
                            <action android:name="com.google.android.c2dm.intent.RECEIVE" />
                        </intent-filter>
                    </receiver>
                    <receiver android:name="androidx.work.impl.background.systemalarm.RescheduleReceiver">
                        <intent-filter>
                            <action android:name="android.intent.action.BOOT_COMPLETED" />
                        </intent-filter>
                    </receiver>
                </application>
            </manifest>
        """

        parseXml(manifest).use { xml ->
            val count = disableStartupWakeReceivers(xml)
            assertEquals(2, count)

            val app = xml.root.children("application").single()
            assertEquals(
                "false",
                app.elementByAttribute("android:name", "com.shopee.app.startup.BootReceiver")
                    .attribute("android:enabled"),
            )
            assertEquals(
                "false",
                app.elementByAttribute("android:name", "com.shopee.app.startup.UpgradeReceiver")
                    .attribute("android:enabled"),
            )
            assertNull(
                app.elementByAttribute("android:name", "com.google.firebase.messaging.FirebaseMessagingReceiver")
                    .attribute("android:enabled"),
            )
            assertNull(
                app.elementByAttribute("android:name", "com.shopee.app.push.HybridPushReceiver")
                    .attribute("android:enabled"),
            )
            assertNull(
                app.elementByAttribute("android:name", "androidx.work.impl.background.systemalarm.RescheduleReceiver")
                    .attribute("android:enabled"),
            )
        }
    }

    @Test
    fun smokeTestMetadataInjectionIsIdempotent() {
        val manifest = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.shopee.ph">
                <application />
            </manifest>
        """

        parseXml(manifest).use { xml ->
            injectSmokeTestMetadata(xml, "true")
            injectSmokeTestMetadata(xml, "v2")

            val app = xml.root.children("application").single()
            val metadataNodes = app.children("meta-data")
                .filter { it.attribute("android:name") == SMOKE_TEST_METADATA_NAME }

            assertEquals(1, metadataNodes.size)
            assertEquals("v2", metadataNodes.single().attribute("android:value"))
        }
    }

    @Test
    fun selectUnusedAbiEntriesStripsOtherAbisAndGuardsSingleAbiApk() {
        val universalEntries = listOf(
            "lib/arm64-v8a/liba.so",
            "lib/armeabi-v7a/liba.so",
            "lib/x86_64/liba.so",
            "assets/index.android.bundle",
        )
        assertEquals(
            listOf("lib/armeabi-v7a/liba.so", "lib/x86_64/liba.so"),
            selectUnusedAbiEntries(universalEntries, "arm64-v8a"),
        )

        val singleAbiEntries = listOf(
            "lib/armeabi-v7a/liba.so",
            "assets/index.android.bundle",
        )
        assertEquals(
            emptyList<String>(),
            selectUnusedAbiEntries(singleAbiEntries, "arm64-v8a"),
        )
    }

    @Test
    fun disablesShopeeTelemetryAndHmsComponentsWhileProtectingTrackerSecurityAndPayments() {
        val manifest = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.shopee.ph">
                <application>
                    <service android:name="com.google.android.gms.tagmanager.TagManagerService" />
                    <service android:name="com.google.android.datatransport.runtime.scheduling.jobscheduling.JobInfoSchedulerService" />
                    <service android:name="com.shopee.luban.module.focuswindow.server.CoreService" />
                    <receiver android:name="com.shopee.app.infra.tracker.util.ShopeeInstallReceiver" />
                    <provider android:name="com.huawei.hms.mlsdk.common.provider.MLInitializerProvider" />
                    <provider android:name="com.shopee.shopeetracker.data.ShopeeTrackerContentProvider" />
                    <service android:name="com.shopee.shpssdk.wvvvuvww" />
                    <service android:name="com.shopee.shopeepaysdk.PayService" />
                    <service android:name="com.google.firebase.messaging.FirebaseMessagingService" />
                    <service android:name="androidx.work.impl.background.systemjob.SystemJobService" />
                </application>
            </manifest>
        """

        parseXml(manifest).use { xml ->
            val count = disableAdAndTelemetryComponents(xml)
            assertEquals(5, count)

            val app = xml.root.children("application").single()
            listOf(
                "com.google.android.gms.tagmanager.TagManagerService",
                "com.google.android.datatransport.runtime.scheduling.jobscheduling.JobInfoSchedulerService",
                "com.shopee.luban.module.focuswindow.server.CoreService",
                "com.shopee.app.infra.tracker.util.ShopeeInstallReceiver",
                "com.huawei.hms.mlsdk.common.provider.MLInitializerProvider",
            ).forEach { name ->
                assertEquals(
                    "false",
                    app.elementByAttribute("android:name", name).attribute("android:enabled"),
                )
            }

            listOf(
                "com.shopee.shopeetracker.data.ShopeeTrackerContentProvider",
                "com.shopee.shpssdk.wvvvuvww",
                "com.shopee.shopeepaysdk.PayService",
                "com.google.firebase.messaging.FirebaseMessagingService",
                "androidx.work.impl.background.systemjob.SystemJobService",
            ).forEach { name ->
                assertNull(
                    app.elementByAttribute("android:name", name).attribute("android:enabled"),
                )
            }
        }
    }

    @Test
    fun deactivatesTelemetryMetadataIdempotentlyAndRemovesNonPushRegistrars() {
        val manifest = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.shopee.ph">
                <application>
                    <meta-data android:name="google_analytics_deferred_deep_link_enabled" android:value="true" />
                    <meta-data android:name="com.facebook.sdk.AutoLogAppEventsEnabled" android:value="false" />
                    <service android:name="com.google.firebase.components.ComponentDiscoveryService">
                        <meta-data
                            android:name="com.google.firebase.components:com.google.firebase.analytics.connector.internal.AnalyticsConnectorRegistrar"
                            android:value="com.google.firebase.components.ComponentRegistrar" />
                        <meta-data
                            android:name="com.google.firebase.components:com.google.firebase.crashlytics.CrashlyticsRegistrar"
                            android:value="com.google.firebase.components.ComponentRegistrar" />
                        <meta-data
                            android:name="com.google.firebase.components:com.google.firebase.datatransport.TransportRegistrar"
                            android:value="com.google.firebase.components.ComponentRegistrar" />
                        <meta-data
                            android:name="com.google.firebase.components:com.google.firebase.messaging.FirebaseMessagingRegistrar"
                            android:value="com.google.firebase.components.ComponentRegistrar" />
                        <meta-data
                            android:name="com.google.firebase.components:com.google.mlkit.vision.barcode.internal.BarcodeRegistrar"
                            android:value="com.google.firebase.components.ComponentRegistrar" />
                    </service>
                </application>
            </manifest>
        """

        parseXml(manifest).use { xml ->
            val firstCount = deactivateTelemetryMetadataAndRegistrars(xml)
            assertEquals(TELEMETRY_METADATA_OVERRIDES.size + 3, firstCount)

            val secondCount = deactivateTelemetryMetadataAndRegistrars(xml)
            assertEquals(TELEMETRY_METADATA_OVERRIDES.size, secondCount)

            val app = xml.root.children("application").single()
            for ((key, expectedValue) in TELEMETRY_METADATA_OVERRIDES) {
                val matching = app.children("meta-data").filter { it.attribute("android:name") == key }
                assertEquals("Expected single meta-data for $key", 1, matching.size)
                assertEquals(expectedValue, matching.single().attribute("android:value"))
            }

            val discoveryService = app.elementByAttribute(
                "android:name",
                "com.google.firebase.components.ComponentDiscoveryService",
            )
            val remainingRegistrars = discoveryService.children("meta-data")
                .mapNotNull { it.attribute("android:name") }
            assertEquals(
                listOf(
                    "com.google.firebase.components:com.google.firebase.messaging.FirebaseMessagingRegistrar",
                    "com.google.firebase.components:com.google.mlkit.vision.barcode.internal.BarcodeRegistrar",
                ),
                remainingRegistrars,
            )
        }
    }

    @Test
    fun disablesVideoLiveAndWidgetBackgroundComponentsWhileKeepingShoppingCore() {
        val manifest = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.shopee.ph">
                <application>
                    <service android:name="com.shopee.sz.luckyvideo.common.utils.download.VideoDownloadService" />
                    <service android:name="com.shopee.live.livestreaming.audience.floatwindow.base.service.FloatVideoService" />
                    <receiver android:name="com.shopee.live.livestreaming.util.NetworkStateReceiver" />
                    <receiver android:name="com.shopee.addon.widgetcheckin.CheckInAppWidget" />
                    <receiver android:name="com.shopee.addon.widgetcheckin.receiver.UpdateWidgetReceiver" />
                    <service android:name="com.shopee.app.biz.noti.impl.pushnotification.fcm.ShopeeFcmMainService" />
                    <receiver android:name="com.shopee.chat.sdk.data.job.FailedBizChatMessageBroadcastReceiver" />
                </application>
            </manifest>
        """

        parseXml(manifest).use { xml ->
            val count = disableVideoAndLiveBackgroundComponents(xml)
            assertEquals(5, count)

            val app = xml.root.children("application").single()
            VIDEO_AND_LIVE_BACKGROUND_COMPONENTS.forEach { name ->
                assertEquals(
                    "false",
                    app.elementByAttribute("android:name", name).attribute("android:enabled"),
                )
            }
            assertNull(
                app.elementByAttribute("android:name", "com.shopee.app.biz.noti.impl.pushnotification.fcm.ShopeeFcmMainService")
                    .attribute("android:enabled"),
            )
            assertNull(
                app.elementByAttribute("android:name", "com.shopee.chat.sdk.data.job.FailedBizChatMessageBroadcastReceiver")
                    .attribute("android:enabled"),
            )
        }
    }

    @Test
    fun trimsInvasivePermissionsWhilePreservingCoreAuthSecurityAndForegroundFeatures() {
        val manifest = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.shopee.ph">
                <uses-permission android:name="android.permission.INTERNET" />
                <uses-permission android:name="android.permission.CAMERA" />
                <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
                <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
                <uses-permission android:name="android.permission.USE_BIOMETRIC" />
                <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
                <uses-permission android:name="android.permission.DETECT_SCREEN_CAPTURE" />
                <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
                <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
                <uses-permission android:name="com.google.android.gms.permission.AD_ID" />
                <uses-permission android:name="android.permission.ACCESS_ADSERVICES_ATTRIBUTION" />
                <uses-permission android:name="android.permission.READ_CALL_LOG" />
                <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
                <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
                <uses-permission android:name="com.sec.android.provider.badge.permission.READ" />
                <uses-permission android:name="com.huawei.android.launcher.permission.WRITE_SETTINGS" />
                <application />
            </manifest>
        """

        parseXml(manifest).use { xml ->
            val removed = trimInvasivePermissions(xml)
            assertEquals(8, removed)

            val remaining = xml.root.children("uses-permission")
                .mapNotNull { it.attribute("android:name") }
            assertEquals(
                listOf(
                    "android.permission.INTERNET",
                    "android.permission.CAMERA",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.POST_NOTIFICATIONS",
                    "android.permission.USE_BIOMETRIC",
                    "android.permission.QUERY_ALL_PACKAGES",
                    "android.permission.DETECT_SCREEN_CAPTURE",
                    "com.google.android.c2dm.permission.RECEIVE",
                ),
                remaining,
            )
            assertNotNull(xml.root.children("application").singleOrNull())
        }
    }
}
