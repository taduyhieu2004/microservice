package com.taskflow.common.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException {

    private final String code;
    private final int status;
    private final Map<String, String> params;

    public BaseException(String code, String message, int status, Map<String, String> params) {
        super(message);
        this.code = code;
        this.status = status;
        this.params = params != null ? new HashMap<>(params) : Collections.emptyMap();
    }

    public BaseException(String code, String message, int status) {
        this(code, message, status, null);
    }
}
