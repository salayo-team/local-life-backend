package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "AI 적성 분석 응답 DTO")
public class AiAptitudeAnalysisResponseDto {

    @Schema(
        description = "AI가 분석한 적성 타입 코드",
        example = "NATURE"
    )
    @JsonProperty("aptitude_type")
    private String aptitudeType;


    @Schema(
        description = "적성 분석 신뢰도 점수 (0.0 ~ 1.0)",
        example = "0.87",
        minimum = "0.0",
        maximum = "1.0"
    )
    @JsonProperty("confidence_score")
    private Double confidenceScore;

    @Schema(
        description = "해당 적성 타입으로 판단한 이유",
        example = "자연 속에서 보내는 시간을 선호하고, 도시보다는 한적한 환경을 선호한다고 여러 차례 언급했습니다."
    )
    @JsonProperty("reason")
    private String reason;

    @Schema(
        description = "AI가 적성을 판단할 때 고려한 핵심 요소 목록",
        example = "[\"자연 활동 선호\", \"도시 생활 피로도\", \"야외 활동 빈도\"]"
    )
    @JsonProperty("key_factors")
    private List<String> keyFactors = new ArrayList<>();
    
    @Builder
    public AiAptitudeAnalysisResponseDto(String aptitudeType, Double confidenceScore,
                                     String reason, List<String> keyFactors) {
        this.aptitudeType = aptitudeType;
        this.confidenceScore = confidenceScore;
        this.reason = reason;

        /**
         * 외부에서 전달받은 객체의 참조를 그대로 사용하지 않고, 새로운 복사본을 만들어 사용 (Defensive Copy)
         */
        this.keyFactors = keyFactors != null ? new ArrayList<>(keyFactors) : new ArrayList<>();
    }
}
