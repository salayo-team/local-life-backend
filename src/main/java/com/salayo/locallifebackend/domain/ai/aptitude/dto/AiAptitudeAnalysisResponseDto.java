package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// AI 적성 분석 응답 DTO
@Getter
@NoArgsConstructor
public class AiAptitudeAnalysisResponseDto {
    
    @JsonProperty("aptitude_type")
    private String aptitudeType; // "NATURE", "ART_CREATION", "HISTORY_CULTURE", "COMMUNITY", "TECH"
    
    @JsonProperty("confidence_score")
    private Double confidenceScore; // 신뢰도 점수 (0.0 ~ 1.0)
    
    @JsonProperty("reason")
    private String reason; // 추천 이유
    
    @JsonProperty("key_factors")
    private List<String> keyFactors = new ArrayList<>(); // 핵심 판단 요소들
    
    @Builder
    public AiAptitudeAnalysisResponseDto(String aptitudeType, Double confidenceScore,
                                     String reason, List<String> keyFactors) {
        this.aptitudeType = aptitudeType;
        this.confidenceScore = confidenceScore;
        this.reason = reason;
        // 외부에서 전달받은 객체의 참조를 그대로 사용하지 않고, 새로운 복사본을 만들어 사용 (Defensive Copy)
        this.keyFactors = keyFactors != null ? new ArrayList<>(keyFactors) : new ArrayList<>();
    }
}
