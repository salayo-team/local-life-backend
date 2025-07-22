package com.salayo.locallifebackend.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RejectReasonRequestDto {

    @NotBlank(message = "거절 사유를 입력해주세요.")
    private String rejectReason;

}
