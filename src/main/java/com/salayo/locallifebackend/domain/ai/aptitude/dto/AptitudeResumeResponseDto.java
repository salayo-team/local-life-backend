package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AptitudeResumeResponseDto {
	private final String sessionId;
	private final Integer nextStep;
	private final Integer totalSteps;
	private final AptitudeQuestionResponseDto nextQuestion;
	private final Integer completedSteps;
	private final String resumeMessage;
	
	@Builder
	public AptitudeResumeResponseDto(String sessionId, Integer nextStep,
		Integer totalSteps, AptitudeQuestionResponseDto nextQuestion,
		Integer completedSteps, String resumeMessage) {
		this.sessionId = sessionId;
		this.nextStep = nextStep;
		this.totalSteps = totalSteps;
		this.nextQuestion = nextQuestion;
		this.completedSteps = completedSteps;
		this.resumeMessage = resumeMessage;
	}
}
