package com.salayo.locallifebackend.domain.email.controller;

import com.salayo.locallifebackend.domain.email.dto.EmailSendRequestDto;
import com.salayo.locallifebackend.domain.email.dto.EmailVerifyRequestDto;
import com.salayo.locallifebackend.domain.email.service.EmailService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Email", description = "이메일 인증 관련 API")
public class EmailController {

    private final EmailService emailService;

    @Operation(
        summary = "이메일 인증 코드 전송",
        description = """
            회원가입 과정에서 사용자의 이메일로 인증 코드를 전송합니다.
            
            - 인증 코드는 6자리 숫자로 구성됩니다.
            - 유효기간은 5분입니다.
            - 이메일이 이미 인증된 상태일 경우 기존 인증 상태는 초기화됩니다.
            - 과도한 요청 발생 시 차단될 수 있습니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 코드 전송 성공",
            content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 이메일 형식"),
        @ApiResponse(responseCode = "429", description = "요청 횟수 초과")
    })
    @PostMapping("/send")
    public ResponseEntity<CommonResponseDto<Void>> sendVerificationCode(@Valid @RequestBody EmailSendRequestDto requestDto) {
        emailService.sendVerificationCode(requestDto.getEmail());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.EMAIL_SEND_SUCCESS, null));
    }

    @Operation(
        summary = "이메일 인증 코드 확인",
        description = """
            사용자가 입력한 이메일과 인증 코드를 비교하여 인증 여부를 확인합니다.
            
            다음과 같은 경우 인증에 실패할 수 있습니다:
            - 인증 코드가 만료됨 (5분 유효)
            - 인증 코드가 존재하지 않음
            - 인증 코드가 일치하지 않음
            
            인증 성공 시:
            - email_verified:{email} 플래그가 Redis에 저장되며, 30분 동안 유효합니다.
            - 인증 코드는 즉시 삭제되어 재사용이 불가능합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 성공",
            content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 인증 코드 또는 이메일 형식"),
        @ApiResponse(responseCode = "404", description = "인증 코드가 존재하지 않거나 만료됨")
    })
    @PostMapping("/verify")
    public ResponseEntity<CommonResponseDto<Void>> verifyEmailCode(@Valid @RequestBody EmailVerifyRequestDto requestDto) {
        emailService.verifyEmailCode(requestDto.getEmail(), requestDto.getCode());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.EMAIL_VERIFY_SUCCESS, null));
    }

}
