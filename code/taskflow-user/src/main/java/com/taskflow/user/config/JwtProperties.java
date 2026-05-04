package com.taskflow.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private TokenConfig accessToken;
    private TokenConfig refreshToken;
    private TokenConfig resetPasswordToken;

    @Data
    public static class TokenConfig {
        private String secretKey;
        private long ttl;
    }
}
