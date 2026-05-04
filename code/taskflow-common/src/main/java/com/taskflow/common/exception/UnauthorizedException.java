package com.taskflow.common.exception;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException() {
        super("unauthorized", "Unauthorized", 401);
    }
    public UnauthorizedException(String code) {
        super(code, "Unauthorized", 401);
    }
}
