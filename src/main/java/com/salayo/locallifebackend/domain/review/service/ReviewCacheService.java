package com.salayo.locallifebackend.domain.review.service;

import com.salayo.locallifebackend.global.util.CacheKeyPrefix;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReviewCacheService {

	private final RedisTemplate<String, String> redisTemplate;

	public ReviewCacheService(@Qualifier("reviewRedisTemplate") RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
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

	/**
	 * 특정 프로그램의 리뷰 캐시를 조회
	 *
	 * @param programId 캐시를 조회할 프로그램의 ID
	 * @return 캐시된 리뷰 데이터 (JSON 문자열). 캐시가 없으면 null을 반환
	 */
	public String getReviewCache(Long programId) {
		String cacheKey = CacheKeyPrefix.REVIEW_PROGRAM + programId;
		return redisTemplate.opsForValue().get(cacheKey);
	}

	/**
	 * 특정 프로그램의 리뷰 데이터를 캐시에 저장
	 *
	 * @param programId  캐시 키로 사용될 프로그램의 ID
	 * @param value      캐시에 저장할 리뷰 데이터 (JSON 문자열)
	 * @param ttlMinutes 캐시의 유효 시간 (분 단위)
	 */
	public void setReviewCache(Long programId, String value, long ttlMinutes) {
		String cacheKey = CacheKeyPrefix.REVIEW_PROGRAM + programId;
		redisTemplate.opsForValue().set(cacheKey, value, ttlMinutes, java.util.concurrent.TimeUnit.MINUTES);
		log.info("리뷰 캐시 저장 - key: {}, ttl: {}분", cacheKey, ttlMinutes);
	}
}
