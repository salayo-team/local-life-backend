package com.salayo.locallifebackend.domain.onboarding.enums;

import lombok.Getter;

/**
 * 온보딩 진행 단계
 */
@Getter
public enum OnboardingStep {
	MEMBER_INFO("회원 정보 입력"),
	REGION_SELECT("선호 지역 특징 선택"),
	APTITUDE_CHECK("적성 인지 여부 확인"),
	APTITUDE_MANUAL("수동 적성 선택"),
	APTITUDE_AI_TEST("AI 적성 검사"),
	COMPLETED("온보딩 완료");

	private final String description;

	OnboardingStep(String description) {
		this.description = description;
	}

	/**
	 * 다음 단계 결정
	 */
	public OnboardingStep getNextStep(boolean knowsAptitude) {
		return switch (this) {
			case MEMBER_INFO -> REGION_SELECT;
			case REGION_SELECT -> APTITUDE_CHECK;
			case APTITUDE_CHECK -> knowsAptitude ? APTITUDE_MANUAL : APTITUDE_AI_TEST;
			case APTITUDE_MANUAL, APTITUDE_AI_TEST -> COMPLETED;
			case COMPLETED -> COMPLETED;
		};
	}
}
