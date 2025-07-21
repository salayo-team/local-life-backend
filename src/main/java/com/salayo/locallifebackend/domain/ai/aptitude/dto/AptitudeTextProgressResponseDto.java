package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import lombok.Getter;

@Getter
public class AptitudeTextProgressResponseDto {

	private final Integer step;
	private final Integer totalStep;
	private final AptitudeQuestionResponseDto aptitudeQuestionResponseDto;
	private final Boolean isComplete;
	private final AptitudeType finalAptitude;

	public AptitudeTextProgressResponseDto(Integer step, Integer totalStep,
		AptitudeQuestionResponseDto aptitudeQuestionResponseDto, Boolean isComplete,
		AptitudeType finalAptitude) {
		this.step = step;
		this.totalStep = totalStep;
		this.aptitudeQuestionResponseDto = aptitudeQuestionResponseDto;
		this.isComplete = isComplete;
		this.finalAptitude = finalAptitude;
	}
}
