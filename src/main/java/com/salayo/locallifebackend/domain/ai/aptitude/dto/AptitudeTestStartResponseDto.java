package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "적성 검사 시작 응답 DTO")
public class AptitudeTestStartResponseDto {

	@Schema(description = "현재 진행 단계", example = "1", minimum = "1", maximum = "5")
	private final Integer step;
	
	@Schema(description = "전체 질문 개수", example = "5")
	private final Integer totalSteps;
	
	@Schema(description = "질문 정보")
	private final AptitudeQuestionResponseDto aptitudeQuestionResponseDto;

	public AptitudeTestStartResponseDto(Integer step, Integer totalSteps, 
			AptitudeQuestionResponseDto aptitudeQuestionResponseDto) {
		this.step = step;
		this.totalSteps = totalSteps;
		this.aptitudeQuestionResponseDto = aptitudeQuestionResponseDto;
	}
}
