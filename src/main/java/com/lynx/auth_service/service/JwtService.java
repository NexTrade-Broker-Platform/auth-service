package com.lynx.auth_service.service;

import com.lynx.auth_service.config.JwtProperties;
import com.lynx.auth_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private Key keyForId(String kid) {
        String secret = jwtProperties.getKeys().get(kid);
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Unknown or unconfigured JWT key id: " + kid);
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        String kid = jwtProperties.getCurrentKeyId();
        return Jwts.builder()
                .setHeaderParam("kid", kid)
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .signWith(keyForId(kid))
                .compact();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUserId(String token) {
        return parseToken(token).getSubject();
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKeyResolver(new SigningKeyResolverAdapter() {
                    @Override
                    public Key resolveSigningKey(JwsHeader header, Claims claims) {
                        String kid = header.getKeyId();
                        // Fall back to the current key for tokens issued before rotation was introduced
                        return keyForId(kid != null ? kid : jwtProperties.getCurrentKeyId());
                    }
                })
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
