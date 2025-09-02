package com.salayo.locallifebackend.domain.ai.aptitude.service;

import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.global.util.CacheKeyPrefix;
import java.util.HashMap;
import java.util.Map;
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
	
	// 답변 저장
	public void saveAnswer(Long memberId, int step, String answer) {
		String key = CacheKeyPrefix.APTITUDE_TEST + memberId + ":answer:" + step;
		redisTemplate.opsForValue().set(key, answer, CacheKeyPrefix.APTITUDE_TEST_TTL_HOURS, TimeUnit.HOURS);
		log.debug("적성 테스트 답변 저장 - memberId: {}, step: {}, answer: {}", memberId, step, answer);
	}
	
	// 적성 점수 저장/업데이트
	public void updateAptitudeScore(Long memberId, AptitudeType aptitudeType, int score) {
		String key = CacheKeyPrefix.APTITUDE_TEST + memberId + ":score:" + aptitudeType.name();
		// 기존 점수가 있으면 누적
		String currentScore = redisTemplate.opsForValue().get(key);
		int newScore = (currentScore != null ? Integer.parseInt(currentScore) : 0) + score;
		
		redisTemplate.opsForValue().set(key, String.valueOf(newScore), CacheKeyPrefix.APTITUDE_TEST_TTL_HOURS, TimeUnit.HOURS);
		log.debug("적성 점수 업데이트 - memberId: {}, aptitudeType: {}, score: {} -> {}", 
			memberId, aptitudeType, currentScore, newScore);
	}
	
	// 모든 적성 점수 조회
	public Map<AptitudeType, Integer> getAllAptitudeScores(Long memberId) {
		Map<AptitudeType, Integer> scores = new HashMap<>();
		
		for (AptitudeType type : AptitudeType.values()) {
			String key = CacheKeyPrefix.APTITUDE_TEST + memberId + ":score:" + type.name();
			String score = redisTemplate.opsForValue().get(key);
			scores.put(type, score != null ? Integer.parseInt(score) : 0);
		}
		
		return scores;
	}
	
	// 모든 테스트 관련 데이터 삭제
	public void deleteAllTestData(Long memberId) {
		// 진행 상태 삭제
		deleteTestProgress(memberId);
		
		// 답변 삭제
		for (int i = 1; i <= CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS; i++) {
			String answerKey = CacheKeyPrefix.APTITUDE_TEST + memberId + ":answer:" + i;
			redisTemplate.delete(answerKey);
		}
		
		// 점수 삭제
		for (AptitudeType type : AptitudeType.values()) {
			String scoreKey = CacheKeyPrefix.APTITUDE_TEST + memberId + ":score:" + type.name();
			redisTemplate.delete(scoreKey);
		}
		
		log.debug("모든 테스트 데이터 삭제 - memberId: {}", memberId);
	}
}
