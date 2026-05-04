package com.taskflow.common.security;

import com.taskflow.common.constant.TaskFlowConstants.Headers;
import com.taskflow.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@UtilityClass
public class SecurityHeaderUtils {

    public static Long currentUserId() {
        HttpServletRequest req = currentRequest();
        String value = req.getHeader(Headers.USER_ID);
        if (value == null || value.isBlank()) {
            throw new UnauthorizedException("missing_user_id_header");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("invalid_user_id_header");
        }
    }

    public static String currentUsername() {
        return header(Headers.USERNAME);
    }

    public static String currentEmail() {
        return header(Headers.USER_EMAIL);
    }

    private static String header(String name) {
        HttpServletRequest req = currentRequest();
        return req.getHeader(name);
    }

    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new UnauthorizedException("no_request_context");
        }
        return attrs.getRequest();
    }
}
