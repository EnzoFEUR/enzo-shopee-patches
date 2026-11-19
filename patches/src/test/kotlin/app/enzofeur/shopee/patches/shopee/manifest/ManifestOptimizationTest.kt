package app.enzofeur.shopee.patches.shopee.manifest

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.readXml
import app.morphe.patcher.xml.ResourceXml
import org.junit.Assert.assertEquals
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
}
