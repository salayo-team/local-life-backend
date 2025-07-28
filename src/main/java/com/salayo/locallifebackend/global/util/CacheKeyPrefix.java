package com.salayo.locallifebackend.global.util;

public final class CacheKeyPrefix {

	// 리뷰 관련 캐시 키 프리픽스
	public static final String REVIEW_PROGRAM = "review:program:"; // 실제 Redis 키 패턴 매칭용
	public static final String PROGRAM_REVIEW_CACHE = "programReviews"; // Spring Cache @Cacheable/@CacheEvict value 이름

	private CacheKeyPrefix() {
		// 인스턴스화를 방지하기 위한 private 생성자
	}
}
