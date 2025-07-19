package com.salayo.locallifebackend.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorSignupRequestDto;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorSignupResponseDto;
import com.salayo.locallifebackend.domain.member.dto.LoginRequestDto;
import com.salayo.locallifebackend.domain.member.dto.LoginResponseDto;
import com.salayo.locallifebackend.domain.member.dto.UserSignupRequestDto;
import com.salayo.locallifebackend.domain.member.dto.UserSignupResponseDto;
import com.salayo.locallifebackend.domain.member.service.AuthService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "회원 인증 관련 API")
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

    @Operation(summary = "로컬크리에이터 회원가입", description = "로컬 크리에이터의 회원가입입니다.")
    @PostMapping(value = "/signup/localcreator", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponseDto<LocalCreatorSignupResponseDto>> signupLocalCreator(
        @RequestPart("data") String requestDtoString,
        @RequestPart("file") List<MultipartFile> files,
        @RequestParam("filePurposes") List<String> filePurposesStrings

    ) {

        ObjectMapper objectMapper = new ObjectMapper();
        LocalCreatorSignupRequestDto requestDto;
        try {
            requestDto = objectMapper.readValue(requestDtoString,
                LocalCreatorSignupRequestDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }

        List<FilePurpose> filePurposes = filePurposesStrings.stream()
            .map(FilePurpose::valueOf)
            .collect(Collectors.toList());

        LocalCreatorSignupResponseDto responseDto = authService.signupLocalCreator(requestDto,
            files, filePurposes);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponseDto.success(SuccessCode.SIGNUP_SUCCESS, responseDto));
    }

    @Operation(summary = "로그인", description = "일반회원/로컬크리에이터 로그인입니다.")
    @PostMapping("/login")
    public ResponseEntity<CommonResponseDto<LoginResponseDto>> login(
        @RequestBody @Valid LoginRequestDto requestDto) {

        LoginResponseDto responseDto = authService.login(requestDto);

        return ResponseEntity.ok(
            CommonResponseDto.success(SuccessCode.LOGIN_SUCCESS, responseDto)
        );

    }

}
