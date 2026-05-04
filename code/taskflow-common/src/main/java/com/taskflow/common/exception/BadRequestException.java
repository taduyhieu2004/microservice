package com.taskflow.common.exception;

import java.util.Map;

public class BadRequestException extends BaseException {

    public BadRequestException(String code) {
        super(code, "Bad Request", 400);
    }

    public BadRequestException(String code, Map<String, String> params) {
        super(code, "Bad Request", 400, params);
    }
}
