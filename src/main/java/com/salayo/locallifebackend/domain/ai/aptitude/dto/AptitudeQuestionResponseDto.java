package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class AptitudeQuestionResponseDto {

	private final Integer order;
	private final String question;
	private final List<String> exampleAnswers;

	public AptitudeQuestionResponseDto(Integer order, String question, List<String> exampleAnswers) {
		this.order = order;
		this.question = question;
		this.exampleAnswers = exampleAnswers;
	}
}
