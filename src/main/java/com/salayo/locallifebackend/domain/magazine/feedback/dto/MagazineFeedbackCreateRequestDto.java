package com.salayo.locallifebackend.domain.magazine.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MagazineFeedbackCreateRequestDto {

    @Size(max = 1000, message = "피드백 내용은 1000자 이내여야 합니다.")
    @NotBlank(message = "피드백 내용은 필수입니다.")
    private String content;

}
