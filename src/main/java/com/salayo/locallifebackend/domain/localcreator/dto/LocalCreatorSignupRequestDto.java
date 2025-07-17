package com.salayo.locallifebackend.domain.localcreator.dto;

import com.salayo.locallifebackend.global.util.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LocalCreatorSignupRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Pattern(regexp = ValidationPatterns.EMAIL_REGEX, message = "올바른 이메일 형식을 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = ValidationPatterns.PASSWORD_REGEX, message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~12자여야 합니다.")
    private String password;

    @NotBlank(message = "출생년도는 필수입니다.")
    @Pattern(regexp = ValidationPatterns.BIRTH_REGEX, message = "출생년도는 YYYY 형식이어야 합니다.")
    private String birth;

    @Pattern(regexp = ValidationPatterns.PHONE_NUMBER_REGEX, message = "휴대폰 번호는 필수입니다.")
    private String phoneNumber;

    @NotBlank(message = "상호명은 필수입니다.")
    private String businessName;

    @NotBlank(message = "사업장주소는 필수입니다.")
    private String businessAddress;

}
