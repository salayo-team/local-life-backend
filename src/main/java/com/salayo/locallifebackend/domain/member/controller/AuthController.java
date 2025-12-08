package com.salayo.locallifebackend.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorSignupRequestDto;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorSignupResponseDto;
import com.salayo.locallifebackend.domain.member.dto.LoginRequestDto;
import com.salayo.locallifebackend.domain.member.dto.LoginResponseDto;
import com.salayo.locallifebackend.domain.member.dto.PasswordResetRequestDto;
import com.salayo.locallifebackend.domain.member.dto.PasswordResetVerifyRequestDto;
import com.salayo.locallifebackend.domain.member.dto.UserSignupRequestDto;
import com.salayo.locallifebackend.domain.member.dto.UserSignupResponseDto;
import com.salayo.locallifebackend.domain.member.service.AuthService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.security.jwt.JwtProvider;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtProvider jwtProvider;

    public AuthController(AuthService authService, JwtProvider jwtProvider) {
        this.authService = authService;
        this.jwtProvider = jwtProvider;
    }

    @Operation(
        summary = "일반회원 회원가입",
        description = """
            일반 사용자의 회원가입 API입니다.
            
            처리 과정:
            - 이메일 인증이 완료된 사용자만 가입이 가능합니다.
            - 닉네임이 비어 있으면 랜덤 닉네임이 자동 생성됩니다.
            - 이메일/닉네임 중복 시 가입이 불가능합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "이메일 인증 미완료 또는 잘못된 입력값"),
        @ApiResponse(responseCode = "409", description = "중복 이메일 또는 닉네임")
    })
    @PostMapping("/signup/user")
    public ResponseEntity<CommonResponseDto<UserSignupResponseDto>> signupUser(
        @RequestBody @Valid UserSignupRequestDto userSignupRequestDto) {

        UserSignupResponseDto responseDto = authService.signupUser(userSignupRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.SIGNUP_SUCCESS, responseDto));
    }

    @Operation(
        summary = "로컬 크리에이터 회원가입",
        description = """
            로컬 크리에이터 회원가입 API입니다.
            multipart/form-data 형식으로 제출되며 다음 정보를 포함합니다.

            - data: LocalCreatorSignupRequestDto(JSON)
            - file: 증빙 파일 리스트
            - filePurposes: 각 파일의 목적 (FilePurpose ENUM)
            
            유효성 조건:
            - 이메일 인증 완료 필수
            - 이메일 중복 불가
            - 파일 개수와 목적 개수 일치 여부 검증
            - 필수 파일(BANK_ACCOUNT_COPY) 누락 시 오류 발생
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "이메일 인증 미완료 / 잘못된 파일 구조"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
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

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.SIGNUP_SUCCESS, responseDto));
    }

    @Operation(
        summary = "로그인",
        description = """
            일반회원 및 로컬 크리에이터의 로그인 API입니다.
            
            - 이메일/비밀번호가 일치해야 합니다.
            - 로컬 크리에이터는 관리자 승인(Approved) 상태여야 로그인 가능합니다.
            - 성공 시 AccessToken 및 RefreshToken을 반환합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 로그인 정보"),
        @ApiResponse(responseCode = "403", description = "로컬 크리에이터 승인 미완료"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResponseDto<LoginResponseDto>> login(
        @RequestBody @Valid LoginRequestDto requestDto) {

        LoginResponseDto responseDto = authService.login(requestDto);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.LOGIN_SUCCESS, responseDto));

    }

    @Operation(
        summary = "로그아웃",
        description = """
            로그아웃 시 사용 중인 AccessToken은 '블랙리스트'로 처리되며,
            토큰이 만료되기 전까지 재사용이 불가능합니다.
            
            처리 과정:
            - RefreshToken 삭제
            - AccessToken 삭제
            - AccessToken을 Redis 블랙리스트에 저장
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 토큰"),
        @ApiResponse(responseCode = "401", description = "만료되거나 유효하지 않은 토큰")
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponseDto<Void>> logout(HttpServletRequest httpServletRequest) {

        String bearerToken = httpServletRequest.getHeader(JwtProvider.AUTH_HEADER);
        String accessToken = jwtProvider.resolveToken(bearerToken);

        authService.logout(accessToken);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.LOGOUT_SUCCESS, null));
    }

    @Operation(
        summary = "비밀번호 재설정 인증코드 발송",
        description = """
            가입된 이메일로 비밀번호 재설정 인증 코드를 전송합니다.
            
            - 인증 코드는 6자리 숫자입니다.
            - 인증 코드는 5분 동안 유효합니다.
            - 인증 성공 전까지 비밀번호 재설정은 불가합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 코드 전송 성공"),
        @ApiResponse(responseCode = "404", description = "해당 이메일의 회원을 찾을 수 없음")
    })
    @PostMapping("/password/send")
    public ResponseEntity<CommonResponseDto<Void>> sendPasswordResetCode(@RequestBody @Valid PasswordResetRequestDto resetRequestDto) {
        authService.sendPasswordResetCode(resetRequestDto.getEmail());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.EMAIL_SEND_SUCCESS, null));
    }

    @Operation(
        summary = "비밀번호 재설정",
        description = """
            비밀번호를 모르는 경우 사용하는 재설정 API입니다.
            
            처리 과정:
            - 이메일로 발송된 인증코드를 입력해야 합니다.
            - 인증코드 일치 시 새 비밀번호로 즉시 변경됩니다.
            - 기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 코드 또는 기존 비밀번호와 동일"),
        @ApiResponse(responseCode = "404", description = "인증 코드 만료 또는 이메일 없음")
    })
    @PostMapping("/password/reset")
    public ResponseEntity<CommonResponseDto<Void>> resetPassword(@RequestBody @Valid PasswordResetVerifyRequestDto resetVerifyRequestDto) {
        authService.resetPassword(resetVerifyRequestDto);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }

}
