package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("question")
    private String question; // 생성된 질문
    
    @JsonProperty("example_answers")
    private List<String> exampleAnswers; // 예시 답변들 (5개)
    
    @JsonProperty("focus_area")
    private String focusArea; // 이 질문이 파악하려는 적성 영역
    
    @Builder
    public AiQuestionGenerationResponseDto(String question, List<String> exampleAnswers, String focusArea) {
        this.question = question;
        this.exampleAnswers = exampleAnswers;
        this.focusArea = focusArea;
    }
}
