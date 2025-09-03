package com.salayo.locallifebackend.domain.magazine.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MagazineFeedbackCreateRequestDto {

    @NotBlank(message = "피드백 내용은 필수입니다.")
    private String content;

}
