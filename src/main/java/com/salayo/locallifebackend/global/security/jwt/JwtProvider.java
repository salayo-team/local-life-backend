package com.salayo.locallifebackend.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class JwtProvider {

    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    @Qualifier("blacklistRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    private final SecretKey key;

    @Getter
    private final long accessTokenExpiry;

    @Getter
    private final long refreshTokenExpiry;

    public JwtProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiry-millis}") long accessTokenExpiry,
        @Value("${jwt.refresh-expiry-millis}") long refreshTokenExpiry,
        @Qualifier("blacklistRedisTemplate")
        RedisTemplate<String, String> redisTemplate
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
        this.redisTemplate = redisTemplate;
    }

    public String generateAccessToken(String email, String role) {
        return generateToken(email, role, accessTokenExpiry);
    }

    public String generateRefreshToken(String email, String role) {
        return generateToken(email, role, refreshTokenExpiry);
    }

    private String generateToken(String email, String role, long expiryTime) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryTime);

        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getPayload().getSubject();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).getPayload().get("role", String.class);
    }

    public String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    public long getExpiration(String token) {
        Date expiration = getClaims(token).getPayload().getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    public void validateTokenOrThrow(String token) {
        if (!validateToken(token)) {
            throw new JwtException("토큰 검증 실패");
        }
    }

    public boolean validateToken(String token) {
        try {
            if (redisTemplate.hasKey(token)) {
                log.warn("블랙리스트 토큰 감지");
                return false;
            }
            getClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            return false;
        }
    }

    private Jws<Claims> getClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        } catch (Exception e) {
            log.error("getClaims 실패: {}", e.getMessage());
            throw e;
        }
    }

}
