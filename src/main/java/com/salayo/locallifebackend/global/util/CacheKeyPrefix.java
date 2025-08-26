package com.salayo.locallifebackend.global.util;

public final class CacheKeyPrefix {

	// 적성 검사 관련 상수
	public static final int APTITUDE_MAX_TEST_COUNT = 5;
	public static final int APTITUDE_TOTAL_QUESTIONS = 5;
	public static final String APTITUDE_TEST = "aptitude:test:";
	public static final long APTITUDE_TEST_TTL_HOURS = 24L;

	// 리뷰 관련 캐시 키 프리픽스
	public static final String REVIEW_PROGRAM = "review:program:";

	public static final String EMAIL_CODE = "auth:email:code:";
	public static final String EMAIL_VERIFIED = "auth:email:verified:";
	public static final String PASSWORD_RESET_CODE = "auth:password:code:";

	public static final String TOKEN_BLACKLIST = "auth:token:blacklist:";
	public static final String ACCESS_TOKEN = "auth:token:access:";
	public static final String REFRESH_TOKEN = "auth:token:refresh:";

	public static final String CREATOR_REAPPLY_TOKEN = "creator:reapply:token:";

	private CacheKeyPrefix() {
		// 인스턴스화를 방지하기 위한 private 생성자
	}
}
