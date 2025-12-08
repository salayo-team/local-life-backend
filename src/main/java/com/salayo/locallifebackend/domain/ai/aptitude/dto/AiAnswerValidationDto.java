package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI가 사용자 답변을 검증할 때 사용하는 내부 DTO
 * AI Service 내부에서 답변 유효성 검증용
 */
@Getter
@NoArgsConstructor
@Schema(description = "AI가 사용자 답변의 유효성을 검증한 결과 DTO")
public class AiAnswerValidationDto {
	
	@JsonProperty("is_valid")
	@Schema(
		description = "답변 유효성 여부. 적성 판단에 활용 가능한 답변이면 true, 너무 짧거나 무관한 경우 false",
		example = "true"
	)
	private boolean isValid;
	
	@JsonProperty("aptitude_type")
	@Schema(
		description = "AI가 판단한 적성 타입. 답변이 유효하지 않은 경우 null 또는 빈 값일 수 있습니다.",
		example = "NATURE",
		allowableValues = { "NATURE", "HISTORY_CULTURE", "ART_CREATION", "COMMUNITY", "TECH" }
	)
	private String aptitudeType;
	
	@JsonProperty("confidence_score")
	@Schema(
		description = "해당 적성 타입에 대한 AI의 신뢰도 점수 (0.0 ~ 1.0)",
		example = "0.82",
		minimum = "0.0",
		maximum = "1.0"
	)
	private double confidenceScore;
	
	@JsonProperty("reason")
	@Schema(
		description = "해당 적성을 선택한 이유 또는 답변이 무효로 판단된 이유에 대한 설명",
		example = "자연, 숲, 바다와 관련된 표현이 반복적으로 등장합니다."
	)
	private String reason;
	
	@JsonProperty("key_factors")
	@Schema(
		description = "판단에 영향을 준 핵심 키워드/요소 목록",
		example = "[\"자연\", \"산책\", \"숲\"]"
	)
	private List<String> keyFactors;
	
	@JsonProperty("follow_up_question")
	@Schema(
		description = "답변이 불충분하거나 무효일 때 추가로 던질 후속 질문. 유효한 답변인 경우 null",
		example = "조금 더 자세히 말해줄래? 예를 들어 주말에 뭐 할 때 가장 행복한지 알려줘."
	)
	private String followUpQuestion;
}
