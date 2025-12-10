package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "중단된 적성 검사 이어하기 응답 DTO")
public class AptitudeResumeResponseDto {

	@Schema(
		description = "적성 검사 세션 ID",
		example = "APT-1-1730901234567"
	)
	private final String sessionId;

	@Schema(
		description = "다음에 진행할 단계 번호",
		example = "3",
		minimum = "1",
		maximum = "5"
	)
	private final Integer nextStep;

	@Schema(
		description = "전체 질문 단계 수",
		example = "5"
	)
	private final Integer totalSteps;

	@Schema(
		description = "다음에 진행할 질문 정보"
	)
	private final AptitudeQuestionResponseDto nextQuestion;

	@Schema(
		description = "지금까지 완료한 단계 수",
		example = "2",
		minimum = "0",
		maximum = "4"
	)
	private final Integer completedSteps;

	@Schema(
		description = "이어하기 안내 메시지",
		example = "3단계부터 이어서 진행합니다."
	)
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
