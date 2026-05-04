package com.taskflow.common.exception;

import java.util.Map;

public class NotFoundException extends BaseException {

    public NotFoundException(String code, String objectName, String id) {
        super(code, "Not Found", 404, Map.of("objectName", objectName, "id", id));
    }

    public NotFoundException(String code) {
        super(code, "Not Found", 404);
    }

    public static NotFoundException of(String objectName, Object id) {
        return new NotFoundException(objectName.toLowerCase() + "_not_found", objectName, String.valueOf(id));
    }
}
