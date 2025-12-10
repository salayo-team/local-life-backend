package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI가 적성 검사 질문을 생성할 때 사용하는 내부 DTO
 * AI Service → Cache Service 데이터 전달용
 */
@Getter
@NoArgsConstructor
@Schema(description = "AI가 생성한 적성 검사 질문 및 예시 답변 DTO")
public class AiQuestionGenerationDto {
	
	@JsonProperty("question")
	@Schema(
		description = "AI가 생성한 질문 내용",
		example = "평소에 어떤 활동을 할 때 가장 즐거워?"
	)
	private String question;
	
	@JsonProperty("example_answers")
	@Schema(
		description = "적성 타입별 예시 답변 목록",
		example = "[{\"aptitude_type\": \"NATURE\", \"example\": \"산책하거나 자연을 느끼는 활동\"}]"
	)
	private List<ExampleAnswer> exampleAnswers;
	
	@Getter
	@NoArgsConstructor
	@Schema(description = "적성 타입별 예시 답변 DTO")
	public static class ExampleAnswer {

		@JsonProperty("aptitude_type")
		@Schema(
			description = "예시 답변이 속하는 적성 타입",
			example = "ART_CREATION",
			allowableValues = { "NATURE", "HISTORY_CULTURE", "ART_CREATION", "COMMUNITY", "TECH" }
		)
		private String aptitudeType;
		
		@JsonProperty("example")
		@Schema(
			description = "해당 적성 타입의 예시 답변 문구",
			example = "무언가 직접 만들고 꾸미는 활동이 좋아."
		)
		private String example;
	}
}
