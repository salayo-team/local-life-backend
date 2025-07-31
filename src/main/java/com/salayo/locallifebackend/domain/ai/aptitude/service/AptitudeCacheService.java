package com.salayo.locallifebackend.domain.ai.aptitude.service;

import com.salayo.locallifebackend.global.util.CacheKeyPrefix;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AptitudeCacheService {

	private final RedisTemplate<String, String> redisTemplate;

	public AptitudeCacheService(@Qualifier("aiAptitudeRedisTemplate") RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// 적성 테스트 진행 상태 저장
	public void saveTestProgress(Long memberId, String step) {
		String key = CacheKeyPrefix.APTITUDE_TEST + memberId;
		redisTemplate.opsForValue().set(key, step, CacheKeyPrefix.APTITUDE_TEST_TTL_HOURS, TimeUnit.HOURS);
		log.debug("적성 테스트 진행 상태 저장 - memberId: {}, step: {}", memberId, step);
	}

	// 적성 테스트 진행 상태 조회
	public String getTestProgress(Long memberId) {
		String key = CacheKeyPrefix.APTITUDE_TEST + memberId;
		return redisTemplate.opsForValue().get(key);
	}

	// 적성 테스트 진행 상태 삭제
	public void deleteTestProgress(Long memberId) {
		String key = CacheKeyPrefix.APTITUDE_TEST + memberId;
		redisTemplate.delete(key);
		log.debug("적성 테스트 진행 상태 삭제 - memberId: {}", memberId);
	}

	// 적성 테스트 진행 상태 업데이트
	public void updateTestProgress(Long memberId, int nextStep) {
		saveTestProgress(memberId, String.valueOf(nextStep));
	}
}
