package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import lombok.Getter;

@Getter
public class AptitudeTestStartResponseDto {

	private final Integer step;
	private final Integer totalSteps;
	private final AptitudeQuestionResponseDto aptitudeQuestionResponseDto;

	public AptitudeTestStartResponseDto(Integer step, Integer totalSteps,
		AptitudeQuestionResponseDto aptitudeQuestionResponseDto) {
		this.step = step;
		this.totalSteps = totalSteps;
		this.aptitudeQuestionResponseDto = aptitudeQuestionResponseDto;
	}
}
