package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI가 사용자 답변을 검증할 때 사용하는 내부 DTO
 * AI Service 내부에서 답변 유효성 검증용
 */
@Getter
@NoArgsConstructor
public class AiAnswerValidationDto {
	
	@JsonProperty("is_valid")
	private boolean isValid;
	
	@JsonProperty("aptitude_type")
	private String aptitudeType;
	
	@JsonProperty("confidence_score")
	private double confidenceScore;
	
	@JsonProperty("reason")
	private String reason;
	
	@JsonProperty("key_factors")
	private List<String> keyFactors;
	
	@JsonProperty("follow_up_question")
	private String followUpQuestion;
}
