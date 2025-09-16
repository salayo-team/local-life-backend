package com.salayo.locallifebackend.domain.onboarding.dto;

import com.salayo.locallifebackend.domain.onboarding.enums.OnboardingStep;
import lombok.Builder;
import lombok.Getter;

/**
 * 온보딩 진행 상태 응답 DTO
 */
@Getter
public class OnboardingProgressResponseDto {
    private final String sessionId;
    private final OnboardingStep currentStep;
    private final OnboardingStep nextStep;
    private final Boolean isCompleted;
    private final String selectedRegion;
    private final Boolean knowsAptitude;
    private final String message;
    
    /**
     * 다음 액션 정보
     */
    private final NextAction nextAction;

    @Builder
	public OnboardingProgressResponseDto(String sessionId, OnboardingStep currentStep, OnboardingStep nextStep, Boolean isCompleted,
		String selectedRegion, Boolean knowsAptitude, String message, NextAction nextAction) {
		this.sessionId = sessionId;
		this.currentStep = currentStep;
		this.nextStep = nextStep;
		this.isCompleted = isCompleted;
		this.selectedRegion = selectedRegion;
		this.knowsAptitude = knowsAptitude;
		this.message = message;
		this.nextAction = nextAction;
	}

	@Getter
    public static class NextAction {
        private final String type;  // "REGION_SELECT", "APTITUDE_CHECK", "APTITUDE_MANUAL", "APTITUDE_AI", "COMPLETE"
        private final String endpoint;
        private final String method;
        private final String description;

        @Builder
		public NextAction(String type, String endpoint, String method, String description) {
			this.type = type;
			this.endpoint = endpoint;
			this.method = method;
			this.description = description;
		}
	}
    
    /**
     * 온보딩 시작 응답 생성
     */
    public static OnboardingProgressResponseDto createStartResponse(String sessionId) {
        return OnboardingProgressResponseDto.builder()
            .sessionId(sessionId)
            .currentStep(OnboardingStep.MEMBER_INFO)
            .nextStep(OnboardingStep.REGION_SELECT)
            .isCompleted(false)
            .message("온보딩을 시작합니다. 선호 지역을 선택해주세요.")
            .nextAction(NextAction.builder()
                .type("REGION_SELECT")
                .endpoint("/api/onboarding/region")
                .method("POST")
                .description("선호 지역 선택")
                .build())
            .build();
    }
    
    /**
     * 지역 선택 완료 응답 생성
     */
    public static OnboardingProgressResponseDto createRegionCompleteResponse(String sessionId, String region) {
        return OnboardingProgressResponseDto.builder()
            .sessionId(sessionId)
            .currentStep(OnboardingStep.REGION_SELECT)
            .nextStep(OnboardingStep.APTITUDE_CHECK)
            .isCompleted(false)
            .selectedRegion(region)
            .message("선호 지역이 설정되었습니다. 적성을 알고 계신가요?")
            .nextAction(NextAction.builder()
                .type("APTITUDE_CHECK")
                .endpoint("/api/onboarding/aptitude-check")
                .method("POST")
                .description("적성 인지 여부 확인")
                .build())
            .build();
    }
    
    /**
     * 적성 확인 완료 응답 생성 (수동 선택)
     */
    public static OnboardingProgressResponseDto createManualAptitudeResponse(String sessionId, String region) {
        return OnboardingProgressResponseDto.builder()
            .sessionId(sessionId)
            .currentStep(OnboardingStep.APTITUDE_CHECK)
            .nextStep(OnboardingStep.APTITUDE_MANUAL)
            .isCompleted(false)
            .selectedRegion(region)
            .knowsAptitude(true)
            .message("적성을 선택해주세요.")
            .nextAction(NextAction.builder()
                .type("APTITUDE_MANUAL")
                .endpoint("/api/ai/aptitude/select")
                .method("POST")
                .description("수동 적성 선택")
                .build())
            .build();
    }
    
    /**
     * 적성 확인 완료 응답 생성 (AI 검사)
     */
    public static OnboardingProgressResponseDto createAiTestResponse(String sessionId, String region) {
        return OnboardingProgressResponseDto.builder()
            .sessionId(sessionId)
            .currentStep(OnboardingStep.APTITUDE_CHECK)
            .nextStep(OnboardingStep.APTITUDE_AI_TEST)
            .isCompleted(false)
            .selectedRegion(region)
            .knowsAptitude(false)
            .message("AI 적성 검사를 시작합니다.")
            .nextAction(NextAction.builder()
                .type("APTITUDE_AI")
                .endpoint("/api/ai/aptitude/test/start")
                .method("POST")
                .description("AI 적성 검사 시작")
                .build())
            .build();
    }
    
    /**
     * 온보딩 완료 응답 생성
     */
    public static OnboardingProgressResponseDto createCompleteResponse(String sessionId, String region) {
        return OnboardingProgressResponseDto.builder()
            .sessionId(sessionId)
            .currentStep(OnboardingStep.COMPLETED)
            .nextStep(null)
            .isCompleted(true)
            .selectedRegion(region)
            .message("온보딩이 완료되었습니다!")
            .nextAction(NextAction.builder()
                .type("COMPLETE")
                .endpoint("/api/home")
                .method("GET")
                .description("홈 화면으로 이동")
                .build())
            .build();
    }
}
