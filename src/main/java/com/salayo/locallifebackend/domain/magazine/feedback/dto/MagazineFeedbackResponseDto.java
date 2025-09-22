package com.salayo.locallifebackend.domain.magazine.feedback.dto;

import com.salayo.locallifebackend.domain.magazine.feedback.entity.MagazineFeedback;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class MagazineFeedbackResponseDto {

    private final Long id;
    private final String content;
    private final int revisionCount;
    private final boolean reflected;
    private final LocalDateTime createdAt;

    public MagazineFeedbackResponseDto(MagazineFeedback feedback) {
        this.id = feedback.getId();
        this.content = feedback.getContent();
        this.revisionCount = feedback.getRevisionCount();
        this.reflected = feedback.isReflected();
        this.createdAt = feedback.getCreatedAt();
    }

}
