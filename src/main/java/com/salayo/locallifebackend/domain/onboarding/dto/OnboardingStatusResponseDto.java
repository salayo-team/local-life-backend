package com.salayo.locallifebackend.domain.onboarding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.salayo.locallifebackend.domain.onboarding.enums.OnboardingStep;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnboardingStatusResponseDto {

	private final boolean isCompleted;
	private final OnboardingStep currentStep;
	private final String sessionId;
}
