package com.salayo.locallifebackend.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MemberWithdrawalRequestDto {

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String currentPassword;

    @NotBlank(message = "탈퇴 사유는 필수입니다.")
    private String reason;

}
