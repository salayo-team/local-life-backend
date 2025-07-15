package com.salayo.locallifebackend.domain.member.dto;

import com.salayo.locallifebackend.global.util.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignupRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Pattern(regexp = ValidationPatterns.EMAIL_REGEX, message = "올바른 이메일 형식을 입력해주세요.")
    private final String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = ValidationPatterns.PASSWORD_REGEX, message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~12자여야 합니다.")
    private final String password;

    @NotBlank(message = "출생년도는 필수입니다.")
    @Pattern(regexp = ValidationPatterns.BIRTH_REGEX, message = "출생년도는 YYYY 형식이어야 합니다.")
    private final String birth;

    private final String nickname;

}
