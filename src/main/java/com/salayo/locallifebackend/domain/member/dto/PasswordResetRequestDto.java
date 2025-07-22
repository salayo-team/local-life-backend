package com.salayo.locallifebackend.domain.member.dto;

import com.salayo.locallifebackend.global.util.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetRequestDto {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Pattern(regexp = ValidationPatterns.EMAIL_REGEX)
    private String email;

}
