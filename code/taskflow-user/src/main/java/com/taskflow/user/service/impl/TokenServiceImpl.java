package com.taskflow.user.service.impl;

import com.taskflow.common.exception.UnauthorizedException;
import com.taskflow.user.config.JwtProperties;
import com.taskflow.user.constant.enums.TokenType;
import com.taskflow.user.entity.User;
import com.taskflow.user.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtProperties props;

    @Override
    public String createToken(User user, TokenType type) {
        Date now = new Date();
        Date expired = new Date(now.getTime() + getTtlMillis(type));

        var builder = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(now)
                .setExpiration(expired);

        if (type == TokenType.ACCESS_TOKEN) {
            builder.claim("username", user.getUsername())
                   .claim("email", user.getEmail());
        }

        return builder.signWith(getSecretKey(type), SignatureAlgorithm.HS256).compact();
    }

    @Override
    public Map<String, Object> parseClaims(String token, TokenType expectedType) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey(expectedType))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims;
        } catch (JwtException ex) {
            log.debug("JWT parse error ({}): {}", expectedType, ex.getMessage());
            throw new UnauthorizedException("invalid_token");
        }
    }

    @Override
    public Long getTtlMillis(TokenType type) {
        return cfg(type).getTtl();
    }

    private Key getSecretKey(TokenType type) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(cfg(type).getSecretKey()));
    }

    private JwtProperties.TokenConfig cfg(TokenType type) {
        return switch (type) {
            case ACCESS_TOKEN -> props.getAccessToken();
            case REFRESH_TOKEN -> props.getRefreshToken();
            case RESET_PASSWORD_TOKEN -> props.getResetPasswordToken();
        };
    }
}
