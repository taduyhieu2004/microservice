package com.taskflow.user.service;

import com.taskflow.user.constant.enums.TokenType;
import com.taskflow.user.entity.User;

import java.util.Map;

public interface TokenService {

    String createToken(User user, TokenType type);

    /** Verify signature + expiration. Throws on invalid. */
    Map<String, Object> parseClaims(String token, TokenType expectedType);

    Long getTtlMillis(TokenType type);
}
