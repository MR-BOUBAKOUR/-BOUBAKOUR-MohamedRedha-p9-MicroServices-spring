package com.MedilaboSolutions.gateway.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AuthUtil {

    // Inject the secret key from application.yml (used to sign and verify JWTs)
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    // Converts the secret string into a key used to sign and verify tokens securely,
    // preventing common issues like weak keys or encoding errors
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateAccessToken(String username, String role, String imageUrl) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", role);
        claims.put("token_type", "access");

        // Add the profile image URL to the token claims only for OAuth2 logins,
        // since only the OAuth2 user have this info; we keep it absent for classic login.
        if (imageUrl != null && !imageUrl.isEmpty()) {
            claims.put("image_url", imageUrl);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh tokens have a longer expiration and are only used to obtain new access tokens
    // We omit sensitive data to keep token size minimal and reduce risk
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("token_type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // No rotation or blackList for this project for now (one user in memory)
    // Just useful to avoid using access tokens in place of refresh tokens during the refresh flow
    public boolean isValidRefreshTokenType(String token) {
        try {
            String tokenType = getAllClaimsFromToken(token).get("token_type", String.class);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getAllClaimsFromToken(String token) {
        return
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private final Cache<String, Claims> tokenCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(10000)
            .build();

    private Claims parseAndValidateToken(String token) {
        log.info("Parsing and validating token (cache MISS) for token: {}", token.substring(0, 10) + "...");
        if (!isValidToken(token)) throw new JwtException("Invalid token");
        return getAllClaimsFromToken(token);
    }

    public Claims getCachedClaims(String token) {
        return tokenCache.get(token, this::parseAndValidateToken);
    }
}
