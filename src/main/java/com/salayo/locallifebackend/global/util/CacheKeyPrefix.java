package com.salayo.locallifebackend.global.util;

public final class CacheKeyPrefix {

	// 리뷰 관련 캐시 키 프리픽스
	public static final String REVIEW_PROGRAM = "review:program:"; // 프로그램별 리뷰 캐시

	private CacheKeyPrefix() {
		// 인스턴스화를 방지하기 위한 private 생성자
	}
}
