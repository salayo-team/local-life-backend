package com.salayo.locallifebackend.domain.onboarding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.salayo.locallifebackend.domain.onboarding.enums.OnboardingStep;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON 응답에서 제외
public class OnboardingStatusResponseDto {

	private final boolean isCompleted;
	private final OnboardingStep currentStep;
	private final String sessionId;

	@Builder
	public OnboardingStatusResponseDto(boolean isCompleted, OnboardingStep currentStep, String sessionId) {
		this.isCompleted = isCompleted;
		this.currentStep = currentStep;
		this.sessionId = sessionId;
	}

	// 온보딩이 완료된 경우를 위한 정적 팩토리 메서드
	public static OnboardingStatusResponseDto complete() {
		return OnboardingStatusResponseDto.builder()
			.isCompleted(true)
			.build();
	}

	// 온보딩 진행 중인 경우를 위한 정적 팩토리 메서드
	public static OnboardingStatusResponseDto inProgress(OnboardingStep currentStep, String sessionId) {
		return OnboardingStatusResponseDto.builder()
			.isCompleted(false)
			.currentStep(currentStep)
			.sessionId(sessionId)
			.build();
	}
}
