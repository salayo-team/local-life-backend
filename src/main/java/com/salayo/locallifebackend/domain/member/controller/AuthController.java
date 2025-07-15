package com.salayo.locallifebackend.domain.member.controller;

import com.salayo.locallifebackend.domain.member.dto.UserSignupRequestDto;
import com.salayo.locallifebackend.domain.member.dto.UserSignupResponseDto;
import com.salayo.locallifebackend.domain.member.service.AuthService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth - 회원 관련", description = "회원 인증 관련 API")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "일반회원 회원가입", description = "일반 사용자의 회원가입입니다.")
    @PostMapping("/signup/user")
    public ResponseEntity<CommonResponseDto<UserSignupResponseDto>> signupUser(
        @RequestBody @Valid UserSignupRequestDto userSignupRequestDto) {

        UserSignupResponseDto responseDto = authService.signupUser(userSignupRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponseDto.success(SuccessCode.SIGNUP_SUCCESS, responseDto));
    }

}
