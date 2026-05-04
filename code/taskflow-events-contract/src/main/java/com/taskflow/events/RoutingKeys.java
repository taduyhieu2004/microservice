package com.taskflow.events;

public final class RoutingKeys {

    private RoutingKeys() {}

    public static final String EXCHANGE = "taskflow.events";
    public static final String DLX = "taskflow.events.dlx";

    // project.*
    public static final String PROJECT_CREATED = "project.created";
    public static final String PROJECT_DELETED = "project.deleted";
    public static final String PROJECT_MEMBER_ADDED = "project.member.added";
    public static final String PROJECT_MEMBER_REMOVED = "project.member.removed";
    public static final String PROJECT_MEMBER_ROLE_CHANGED = "project.member.role_changed";

    // board.* / list.*
    public static final String BOARD_CREATED = "board.created";
    public static final String BOARD_DELETED = "board.deleted";
    public static final String LIST_CREATED = "list.created";
    public static final String LIST_DELETED = "list.deleted";

    // task.*
    public static final String TASK_CREATED = "task.created";
    public static final String TASK_UPDATED = "task.updated";
    public static final String TASK_MOVED = "task.moved";
    public static final String TASK_ASSIGNED = "task.assigned";
    public static final String TASK_DELETED = "task.deleted";
    public static final String TASK_DUE_SOON = "task.due_soon";
    public static final String TASK_OVERDUE = "task.overdue";
    public static final String TASK_DEPENDENCY_CHANGED = "task.dependency.changed";

    // comment.* / attachment.*
    public static final String COMMENT_ADDED = "comment.added";
    public static final String ATTACHMENT_UPLOADED = "attachment.uploaded";

    // Patterns for binding
    public static final String PATTERN_TASK_ALL = "task.*";
    public static final String PATTERN_PROJECT_ALL = "project.*";
    public static final String PATTERN_PROJECT_MEMBER = "project.member.*";
    public static final String PATTERN_BOARD_ALL = "board.*";
    public static final String PATTERN_LIST_ALL = "list.*";
    public static final String PATTERN_COMMENT_ALL = "comment.*";
    public static final String PATTERN_ATTACHMENT_ALL = "attachment.*";
}
