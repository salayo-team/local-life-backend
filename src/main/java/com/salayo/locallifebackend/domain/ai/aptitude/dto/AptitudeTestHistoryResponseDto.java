package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "적성 검사 이력 응답 DTO")
public class AptitudeTestHistoryResponseDto {

    @Schema(
        description = "적성 검사 이력 ID",
        example = "1"
    )
    private final Long historyId;

    @Schema(
        description = "질문 단계 번호",
        example = "2",
        minimum = "1",
        maximum = "5"
    )
    private final Integer step;

    @Schema(
        description = "질문 내용",
        example = "일과 후에 가장 자주 하는 활동은 무엇인가요?"
    )
    private final String questionText;

    @Schema(
        description = "사용자가 입력한 답변",
        example = "사람이 많지 않은 공원을 걸으면서 음악을 듣는 편이에요."
    )
    private final String userResponse;

    @Schema(
        description = "AI가 분석한 결과 요약 문자열",
        example = "NATURE - 자연 친화 활동 선호 (신뢰도: 0.82)"
    )
    private final String aiResponse;

    @Schema(
        description = "AI가 분석한 적성 타입",
        example = "NATURE"
    )
    private final AptitudeType aptitudeType;

    @Schema(
        description = "이 단계에서의 적성 판단 신뢰도 점수",
        example = "0.82",
        minimum = "0.0",
        maximum = "1.0"
    )
    private final Double confidenceScore;

    @Schema(
        description = "적성 검사 세션 ID",
        example = "APT-1-1730901234567"
    )
    private final String sessionId;

    @Schema(
        description = "테스트 완료 여부",
        example = "true"
    )
    private final Boolean isCompleted;

    @Schema(
        description = "이력이 생성된 시각(한국 시간 기준)",
        example = "2025-11-08T01:23:45"
    )
    private final LocalDateTime createdAt;
    
    @Builder
    public AptitudeTestHistoryResponseDto(Long historyId, Integer step, String questionText,
                                         String userResponse, String aiResponse, AptitudeType aptitudeType,
                                         Double confidenceScore, String sessionId, Boolean isCompleted,
                                         LocalDateTime createdAt) {
        this.historyId = historyId;
        this.step = step;
        this.questionText = questionText;
        this.userResponse = userResponse;
        this.aiResponse = aiResponse;
        this.aptitudeType = aptitudeType;
        this.confidenceScore = confidenceScore;
        this.sessionId = sessionId;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }
}
