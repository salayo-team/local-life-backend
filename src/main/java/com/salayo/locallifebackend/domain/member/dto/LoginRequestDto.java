package com.salayo.locallifebackend.domain.member.dto;

import com.salayo.locallifebackend.global.util.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {

    @NotBlank
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Pattern(regexp = ValidationPatterns.EMAIL_REGEX)
    private String email;

    @NotBlank
    @Pattern(regexp = ValidationPatterns.PASSWORD_REGEX,
        message = "비밀번호는 영문 소문자, 숫자, 특수문자를 포함한 8~12자여야 합니다.")
    private String password;

}
