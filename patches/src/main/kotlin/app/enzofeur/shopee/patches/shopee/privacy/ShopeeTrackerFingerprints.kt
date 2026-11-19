package app.enzofeur.shopee.patches.shopee.privacy

import app.morphe.patcher.Fingerprint

private const val SHOPEE_TRACKER_CLASS = "Lcom/shopee/shopeetracker/ShopeeTracker;"
private const val EVENT_LOGGER_CLASS = "Lcom/shopee/shopeetracker/eventhandler/EventLogger;"
private const val EVENT_SCHEDULER_CLASS = "Lcom/shopee/shopeetracker/eventhandler/EventSendScheduler;"
private const val EVENT_SENDER_MANAGER_CLASS = "Lcom/shopee/shopeetracker/eventhandler/EventSenderManager;"
private const val MMAP_PROCESSOR_CLASS = "Lcom/shopee/shopeetracker/data/mmap/MMAPProcessor;"

internal val ShopeeTrackerIsEnabledFingerprint = Fingerprint(
    definingClass = SHOPEE_TRACKER_CLASS,
    name = "isEnabled",
    returnType = "Z",
    parameters = listOf(),
)

internal val ShopeeTrackerIsMonitorEnabledFingerprint = Fingerprint(
    definingClass = SHOPEE_TRACKER_CLASS,
    name = "isMonitorEnabled",
    returnType = "Z",
    parameters = listOf(),
)

internal val EventLoggerSyncLogDataStringFingerprint = Fingerprint(
    definingClass = EVENT_LOGGER_CLASS,
    name = "syncLogDataByType",
    returnType = "Z",
    parameters = listOf("I", "Ljava/lang/String;"),
)

internal val MmapProcessorCanUseFingerprint = Fingerprint(
    definingClass = MMAP_PROCESSOR_CLASS,
    name = "canUse",
    returnType = "Z",
    parameters = listOf(),
)

internal val SHOPEE_TRACKER_FALSE_FINGERPRINTS = listOf(
    ShopeeTrackerIsEnabledFingerprint,
    ShopeeTrackerIsMonitorEnabledFingerprint,
    EventLoggerSyncLogDataStringFingerprint,
    MmapProcessorCanUseFingerprint,
)

internal val ShopeeTrackerDelayTaskFingerprint = Fingerprint(
    definingClass = SHOPEE_TRACKER_CLASS,
    name = "delayTask",
    returnType = "V",
    parameters = listOf(),
)

internal val ShopeeTrackerNotifyToUploadFingerprint = Fingerprint(
    definingClass = SHOPEE_TRACKER_CLASS,
    name = "notifyToUpLoad",
    returnType = "V",
    parameters = listOf(),
)

internal val EventLoggerLogActionFingerprint = Fingerprint(
    definingClass = EVENT_LOGGER_CLASS,
    name = "logAction",
    returnType = "V",
    parameters = listOf("L"),
)

internal val EventLoggerLogUserBehaviorBuilderFingerprint = Fingerprint(
    definingClass = EVENT_LOGGER_CLASS,
    name = "logUserBehaviorBuilder",
    returnType = "V",
    parameters = listOf("L"),
)

internal val EventLoggerLogRealtimeUserBehaviorBuilderFingerprint = Fingerprint(
    definingClass = EVENT_LOGGER_CLASS,
    name = "logRealtimeUserBehaviorBuilder",
    returnType = "V",
    parameters = listOf("L"),
)

internal val EventLoggerSyncLogActionFingerprint = Fingerprint(
    definingClass = EVENT_LOGGER_CLASS,
    name = "syncLogAction",
    returnType = "V",
    parameters = listOf("L"),
)

internal val EventLoggerSyncLogDataListFingerprint = Fingerprint(
    definingClass = EVENT_LOGGER_CLASS,
    name = "syncLogDataByType",
    returnType = "V",
    parameters = listOf("I", "Ljava/util/List;"),
)

internal val EventLoggerSyncLogUserBehaviorBuilderFingerprint = Fingerprint(
    definingClass = EVENT_LOGGER_CLASS,
    name = "syncLogUserBehaviorBuilder",
    returnType = "V",
    parameters = listOf("L"),
)

internal val EventSendSchedulerStartScheduleFingerprint = Fingerprint(
    definingClass = EVENT_SCHEDULER_CLASS,
    name = "startSchedule",
    returnType = "V",
    parameters = listOf(),
)

internal val EventSendSchedulerSendImmediatelyFingerprint = Fingerprint(
    definingClass = EVENT_SCHEDULER_CLASS,
    name = "sendDataImmediately",
    returnType = "V",
    parameters = listOf(),
)

internal val EventSendSchedulerUpdateFingerprint = Fingerprint(
    definingClass = EVENT_SCHEDULER_CLASS,
    name = "update",
    returnType = "V",
    parameters = listOf("J"),
)

internal val EventSenderManagerSendEventsFingerprint = Fingerprint(
    definingClass = EVENT_SENDER_MANAGER_CLASS,
    name = "sendEvents",
    returnType = "V",
    parameters = listOf(),
)

internal val EventSenderManagerSendEventByTypeFingerprint = Fingerprint(
    definingClass = EVENT_SENDER_MANAGER_CLASS,
    name = "sendEventByType",
    returnType = "V",
    parameters = listOf("I"),
)

internal val EventSenderManagerSendDirectlyFingerprint = Fingerprint(
    definingClass = EVENT_SENDER_MANAGER_CLASS,
    name = "sendDirectly",
    returnType = "V",
    parameters = listOf("I", "Ljava/util/List;"),
)

internal val SHOPEE_TRACKER_VOID_FINGERPRINTS = listOf(
    ShopeeTrackerDelayTaskFingerprint,
    ShopeeTrackerNotifyToUploadFingerprint,
    EventLoggerLogActionFingerprint,
    EventLoggerLogUserBehaviorBuilderFingerprint,
    EventLoggerLogRealtimeUserBehaviorBuilderFingerprint,
    EventLoggerSyncLogActionFingerprint,
    EventLoggerSyncLogDataListFingerprint,
    EventLoggerSyncLogUserBehaviorBuilderFingerprint,
    EventSendSchedulerStartScheduleFingerprint,
    EventSendSchedulerSendImmediatelyFingerprint,
    EventSendSchedulerUpdateFingerprint,
    EventSenderManagerSendEventsFingerprint,
    EventSenderManagerSendEventByTypeFingerprint,
    EventSenderManagerSendDirectlyFingerprint,
)
