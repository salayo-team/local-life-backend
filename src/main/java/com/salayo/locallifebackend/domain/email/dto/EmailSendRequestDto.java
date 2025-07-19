package com.salayo.locallifebackend.domain.email.dto;

import com.salayo.locallifebackend.global.util.ValidationPatterns;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailSendRequestDto {

    @Pattern(regexp = ValidationPatterns.EMAIL_REGEX, message = "올바른 이메일 형식이 아닙니다.")
    private String email;

}
