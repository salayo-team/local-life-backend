package com.salayo.locallifebackend.domain.onboarding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.onboarding.enums.OnboardingStep;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "온보딩 진행 상태 조회 응답 DTO")
public class OnboardingStatusResponseDto {

	@Schema(description = "온보딩 완료 여부", example = "false")
	private final boolean isCompleted;

	@Schema(description = "현재 온보딩 단계", example = "REGION_SELECT")
	private final OnboardingStep currentStep;

	@Schema(description = "온보딩 세션 ID", example = "ONB-1-ab12cd34")
	private final String sessionId;

	@Schema(description = "사용자의 적성 타입 (온보딩 미완료 시 PENDING)",
		example = "NATURE")
	private final AptitudeType aptitudeType;
}
