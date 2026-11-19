package app.enzofeur.shopee.patches.shopee.performance

import app.enzofeur.shopee.patches.shared.Constants.COMPATIBILITY_SHOPEE_PH
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.patch
import app.morphe.patcher.util.returnEarly

@Suppress("unused")
val disableStartupTelemetryValvesPatch = patch(
    name = "Disable cold-start APM and profiler valves",
    description = "Skips non-critical cold-start APM, Firebase Analytics, logging pipeline valves, and Luban/StackLens background sampling to cut startup latency, CPU usage, and battery drain.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SHOPEE_PH)

    execute {
        var matched = 0

        STARTUP_TELEMETRY_VALVE_FINGERPRINTS.forEach { fingerprint ->
            fingerprint.methodOrNull?.let {
                it.returnEarly()
                matched++
            }
        }

        if (matched == 0) {
            throw PatchException("No Shopee startup telemetry/APM target methods matched.")
        }
    }
}
