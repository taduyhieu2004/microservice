package com.taskflow.common.exception;

import java.util.Map;

public class ConflictException extends BaseException {
    public ConflictException(String code) {
        super(code, "Conflict", 409);
    }
    public ConflictException(String code, Map<String, String> params) {
        super(code, "Conflict", 409, params);
    }
}
