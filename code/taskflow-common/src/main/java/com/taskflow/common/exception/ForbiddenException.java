package com.taskflow.common.exception;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String code) {
        super(code, "Forbidden", 403);
    }
    public ForbiddenException() {
        super("forbidden", "Forbidden", 403);
    }
}
