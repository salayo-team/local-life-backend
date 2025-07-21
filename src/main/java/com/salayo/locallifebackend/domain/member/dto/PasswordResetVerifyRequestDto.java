package com.salayo.locallifebackend.domain.member.dto;

import com.salayo.locallifebackend.global.util.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetVerifyRequestDto {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Pattern(regexp = ValidationPatterns.EMAIL_REGEX)
    private String email;

    @NotBlank(message = "인증 코드를 입력해주세요.")
    private String code;

    @NotBlank(message = "새로운 비밀번호를 입력해주세요")
    @Pattern(regexp = ValidationPatterns.PASSWORD_REGEX)
    private String newPassword;

}
