package app.enzofeur.shopee.patches.shopee.privacy

import app.morphe.patcher.Fingerprint

internal val AptLogReuploadDoWorkFingerprint = Fingerprint(
    definingClass = "Lcom/shopee/logging/aptlog/business/work/AptLogReuploadWorker;",
    name = "doWork",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lkotlin/coroutines/f;"),
)

internal val DataPointTriggerRunFingerprint = Fingerprint(
    definingClass =
        "Lcom/shopee/app/infra/tracker/util/datapoint/base/triggerSource/a0;",
    name = "a",
    returnType = "V",
    parameters = listOf(),
)

internal val BundleSyncDoInBackgroundFingerprint = Fingerprint(
    definingClass = "Lcom/shopee/app/container/rn/react/sync/BundleSyncService\$a;",
    name = "doInBackground",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("[Ljava/lang/Object;"),
)
