package com.taskflow.common.exception;

public class InternalServerException extends BaseException {
    public InternalServerException() {
        super("internal_server_error", "Internal Server Error", 500);
    }
    public InternalServerException(String code) {
        super(code, "Internal Server Error", 500);
    }
}
