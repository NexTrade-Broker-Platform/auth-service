package com.lynx.auth_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String currentKeyId;
    private Map<String, String> keys = new HashMap<>();
    private long expirationMs;
}
