package com.taskflow.common.constant;

public final class TaskFlowConstants {

    private TaskFlowConstants() {}

    public static final class Headers {
        public static final String USER_ID = "X-User-Id";
        public static final String USERNAME = "X-Username";
        public static final String USER_EMAIL = "X-User-Email";
        public static final String TRACE_ID = "X-Trace-Id";
        public static final String IDEMPOTENCY_KEY = "Idempotency-Key";
        public static final String LANGUAGE = "Accept-Language";
        public static final String DEFAULT_LANGUAGE = "vi";
        private Headers() {}
    }

    public static final class Cache {
        public static final long PROJECT_ROLE_TTL_SECONDS = 300;
        public static final long IDEMPOTENCY_TTL_SECONDS = 86_400;
        public static final long USER_CONTACT_TTL_SECONDS = 600;
        private Cache() {}
    }

    public static final class Limits {
        public static final long MAX_FILE_SIZE_BYTES = 25L * 1024 * 1024;
        public static final int MAX_TASKS_PER_LIST = 1000;
        private Limits() {}
    }

    public static final class TokenPrefix {
        public static final String JWT = "jwt:";
        public static final String IDEM = "idem:";
        public static final String ROLE = "role:";
        private TokenPrefix() {}
    }
}
