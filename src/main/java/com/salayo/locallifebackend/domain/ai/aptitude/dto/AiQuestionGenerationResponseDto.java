package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 질문 생성 응답 DTO
 */
@Getter
@NoArgsConstructor
public class AiQuestionGenerationResponseDto {

    @Schema(
        description = "AI가 생성한 질문 내용",
        example = "하루 중 가장 에너지가 많이 채워지는 순간은 언제인가요?"
    )
    @JsonProperty("question")
    private String question;

    @Schema(
        description = "해당 질문에 대한 예시 답변 목록",
        example = "[\"숲길을 산책할 때\", \"새로운 사람을 만날 때\", \"작업실에서 무언가를 만들 때\", \"도시의 카페를 돌아다닐 때\", \"새로운 도구나 앱을 테스트할 때\"]"
    )
    @JsonProperty("example_answers")
    private List<String> exampleAnswers;

    @Schema(
        description = "이 질문이 집중해서 파악하려는 적성 영역",
        example = "NATURE"
    )
    @JsonProperty("focus_area")
    private String focusArea;
    
    @Builder
    public AiQuestionGenerationResponseDto(String question, List<String> exampleAnswers, String focusArea) {
        this.question = question;
        this.exampleAnswers = exampleAnswers;
        this.focusArea = focusArea;
    }
}
