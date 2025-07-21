package com.salayo.locallifebackend.domain.member.dto;

import com.salayo.locallifebackend.global.util.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordUpdateRequestDto {

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    @Pattern(regexp = ValidationPatterns.PASSWORD_REGEX)
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Pattern(regexp = ValidationPatterns.PASSWORD_REGEX)
    private String newPassword;

}
