package com.salayo.locallifebackend.domain.email.controller;

import com.salayo.locallifebackend.domain.email.dto.EmailSendRequestDto;
import com.salayo.locallifebackend.domain.email.dto.EmailVerifyRequestDto;
import com.salayo.locallifebackend.domain.email.service.EmailService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/email")
@Tag(name = "Email", description = "이메일 관련 API")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(summary = "회원가입 시 이메일 인증 코드 전송", description = "회원가입 시 사용자의 이메일로 인증 코드를 전송합니다. 인증코드는 5분간 유효합니다.")
    public ResponseEntity<CommonResponseDto<Void>> sendVerificationCode(@RequestBody EmailSendRequestDto requestDto) {
        emailService.sendVerificationCode(requestDto.getEmail());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.EMAIL_SEND_SUCCESS, null));
    }

    @PostMapping("/verify")
    @Operation(summary = "회원가입 시 이메일 인증 코드 확인", description = "회원가입 시 이메일과 인증 코드를 비교하여 인증 여부를 확인합니다.")
    public ResponseEntity<CommonResponseDto<Void>> verifyEmailCode(@Valid @RequestBody EmailVerifyRequestDto requestDto) {
        emailService.verifyEmailCode(requestDto.getEmail(), requestDto.getCode());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.EMAIL_VERIFY_SUCCESS, null));
    }

}
