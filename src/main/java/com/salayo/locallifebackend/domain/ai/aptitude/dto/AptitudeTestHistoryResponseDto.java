package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestHistory;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AptitudeTestHistoryResponseDto {
    
    private final Long historyId;
    private final Integer step;
    private final String questionText;
    private final String userResponse;
    private final String aiResponse;
    private final String aptitudeType;
    private final Double confidenceScore;
    private final String sessionId;
    private final Boolean isCompleted;
    private final LocalDateTime createdAt;
    
    @Builder
    public AptitudeTestHistoryResponseDto(Long historyId, Integer step, String questionText,
                                         String userResponse, String aiResponse, String aptitudeType,
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
