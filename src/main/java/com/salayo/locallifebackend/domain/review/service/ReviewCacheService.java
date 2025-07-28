package com.salayo.locallifebackend.domain.review.service;

import com.salayo.locallifebackend.global.util.CacheKeyPrefix;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewCacheService {

	@Qualifier("reviewRedisTemplate")
	private final RedisTemplate<String, String> redisTemplate;

	// 프로그램 리뷰 캐시 무효화 단일 키 삭제 방식
	public void invalidateReviewCache(Long programId) {
		String cacheKey = CacheKeyPrefix.REVIEW_PROGRAM + programId;
		redisTemplate.delete(cacheKey);
		log.info("리뷰 캐시 무효화 - key: {}", cacheKey);
	}

	/**
	 * 프로그램 리뷰 캐시 무효화 (패턴 매칭) 페이지네이션, 정렬 옵션 등 여러 캐시 키가 있을 경우 사용
	 * <p>
	 * 주의: keys() 명령어는 프로덕션에서 성능 이슈가 있을 수 있음 대안: SCAN 명령어 사용 또는 캐시 키 목록 별도 관리
	 */
	public void invalidateReviewCacheByPattern(Long programId) {
		String pattern = CacheKeyPrefix.REVIEW_PROGRAM + programId + "*";
		Set<String> keys = redisTemplate.keys(pattern);

		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
			log.info("리뷰 캐시 무효화 (패턴 매칭) - pattern: {}, count: {}", pattern, keys.size());
		} else {
			log.debug("리뷰 캐시 무효화 (패턴 매칭) - 삭제할 키 없음 - pattern: {}", pattern);
		}
	}

	// 리뷰 캐시 조회
	public String getReviewCache(Long programId) {
		String cacheKey = CacheKeyPrefix.REVIEW_PROGRAM + programId;
		return redisTemplate.opsForValue().get(cacheKey);
	}

	// 리뷰 캐시 저장
	public void setReviewCache(Long programId, String value, long ttlMinutes) {
		String cacheKey = CacheKeyPrefix.REVIEW_PROGRAM + programId;
		redisTemplate.opsForValue().set(cacheKey, value, ttlMinutes, java.util.concurrent.TimeUnit.MINUTES);
		log.info("리뷰 캐시 저장 - key: {}, ttl: {}분", cacheKey, ttlMinutes);
	}
}
