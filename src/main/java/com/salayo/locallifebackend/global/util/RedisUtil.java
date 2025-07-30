package com.salayo.locallifebackend.global.util;

import static com.salayo.locallifebackend.global.util.CacheKeyPrefix.ACCESS_TOKEN;
import static com.salayo.locallifebackend.global.util.CacheKeyPrefix.REFRESH_TOKEN;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(Long memberId, String token, long expirationMillis) {
        String key = REFRESH_TOKEN + memberId;
        redisTemplate.opsForValue().set(key, token, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN + memberId;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN + memberId;
        redisTemplate.delete(key);
    }

    public void saveAccessToken(Long memberId, String token, long expirationMillis) {
        String key = ACCESS_TOKEN + memberId;
        redisTemplate.opsForValue().set(key, token, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public String getAccessToken(Long memberId) {
        String key = ACCESS_TOKEN + memberId;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteAccessToken(Long memberId) {
        String key = ACCESS_TOKEN + memberId;
        redisTemplate.delete(key);
    }

}
