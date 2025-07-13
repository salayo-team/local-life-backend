package com.salayo.locallifebackend.domain.email.dto;

import com.salayo.locallifebackend.global.util.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerifyRequestDto {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Pattern(regexp = ValidationPatterns.EMAIL_REGEX, message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "인증 코드는 필수 입력값입니다.")
    @Pattern(regexp = ValidationPatterns.CODE_REGEX, message = "인증 코드는 6자리 숫자여야 합니다.")
    private String code;
}
