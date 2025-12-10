package com.salayo.locallifebackend.domain.onboarding.dto;

import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.onboarding.enums.OnboardingStep;
import com.salayo.locallifebackend.domain.onboarding.enums.RegionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "온보딩 전체 진행 상태 응답 DTO")
public class OnboardingProgressResponseDto {

	@Schema(description = "온보딩 세션 ID",
		example = "ONB-1-ab12cd34")
	@ToString.Exclude
	private final String sessionId;

	@Schema(description = "현재 온보딩 단계",
		example = "REGION_SELECT")
	private final OnboardingStep currentStep;

	@Schema(description = "다음 온보딩 단계 (마지막 단계면 null)",
		example = "APTITUDE_CHECK")
	private final OnboardingStep nextStep;

	@Schema(description = "온보딩 완료 여부",
		example = "false")
	private final Boolean isCompleted;

	@Schema(description = "사용자가 선택한 지역 특징 타입 (초기 단계에서는 null)",
		example = "URBAN")
	private final RegionType regionType;

	@Schema(description = "적성 타입 (초기 단계에서는 PENDING)",
		example = "PENDING")
	private final AptitudeType aptitudeType;

	@Schema(description = "사용자가 적성을 알고 있다고 응답했는지 여부 (AI 검사와 수동 선택 분기용)",
		example = "false")
	private final Boolean knowsAptitude;

	@Schema(description = "현재 단계에 대한 안내 메시지",
		example = "온보딩을 시작합니다. 선호 지역을 선택해주세요.")
	private final String guideMessage;

	@Schema(description = "다음 단계 이동을 위한 액션 정보")
	private final NextAction nextAction;

	@Builder
	public OnboardingProgressResponseDto(String sessionId, OnboardingStep currentStep, OnboardingStep nextStep, Boolean isCompleted,
		RegionType regionType, AptitudeType aptitudeType, Boolean knowsAptitude, String guideMessage, NextAction nextAction) {
		this.sessionId = sessionId;
		this.currentStep = currentStep;
		this.nextStep = nextStep;
		this.isCompleted = isCompleted;
		this.regionType = regionType;
		this.aptitudeType = aptitudeType;
		this.knowsAptitude = knowsAptitude;
		this.guideMessage = guideMessage;
		this.nextAction = nextAction;
	}

	@Getter
	@Schema(description = "온보딩 다음 액션 정보 DTO")
	public static class NextAction {

		@Schema(description = "다음 온보딩 단계",
			example = "REGION_SELECT")
		private final OnboardingStep type;

		@Schema(description = "다음 호출해야 하는 API 엔드포인트",
			example = "/onboarding/region")
		private final String endpoint;

		@Schema(description = "HTTP 메서드",
			example = "POST")
		private final String method;

		@Schema(description = "다음 단계에 대한 설명",
			example = "선호 지역 특징 선택")
		private final String description;

		@Builder
		public NextAction(OnboardingStep type, String endpoint, String method, String description) {
			this.type = type;
			this.endpoint = endpoint;
			this.method = method;
			this.description = description;
		}
	}

}
