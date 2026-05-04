package com.taskflow.events;

public final class Queues {

    private Queues() {}

    public static final String NOTIFICATION = "notification.q";
    public static final String NOTIFICATION_DLQ = "notification.q.dlq";

    public static final String COLLAB_ACTIVITY = "collab.activity.q";
    public static final String COLLAB_ACTIVITY_DLQ = "collab.activity.q.dlq";

    public static final String COLLAB_CLEANUP = "collab.cleanup.q";
    public static final String COLLAB_CLEANUP_DLQ = "collab.cleanup.q.dlq";

    public static final String TASK_CLEANUP = "task.cleanup.q";
    public static final String TASK_CLEANUP_DLQ = "task.cleanup.q.dlq";
}
