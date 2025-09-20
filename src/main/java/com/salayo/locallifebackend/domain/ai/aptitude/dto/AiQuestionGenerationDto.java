package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI가 적성 검사 질문을 생성할 때 사용하는 내부 DTO
 * AI Service → Cache Service 데이터 전달용
 */
@Getter
@NoArgsConstructor
public class AiQuestionGenerationDto {
	
	@JsonProperty("question")
	private String question;
	
	@JsonProperty("example_answers")
	private List<ExampleAnswer> exampleAnswers;
	
	@Getter
	@NoArgsConstructor
	public static class ExampleAnswer {
		@JsonProperty("aptitude_type")
		private String aptitudeType;
		
		@JsonProperty("example")
		private String example;
	}
}
