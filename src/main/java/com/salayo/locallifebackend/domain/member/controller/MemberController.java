package com.salayo.locallifebackend.domain.member.controller;

import com.salayo.locallifebackend.domain.member.dto.MemberInfoResponseDto;
import com.salayo.locallifebackend.domain.member.dto.MemberUpdateRequestDto;
import com.salayo.locallifebackend.domain.member.dto.MemberWithdrawalRequestDto;
import com.salayo.locallifebackend.domain.member.dto.PasswordUpdateRequestDto;
import com.salayo.locallifebackend.domain.member.service.MemberService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/me")
@Tag(name = "Member", description = "회원 개인 정보 관련 API")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(
        summary = "비밀번호 변경 (마이페이지)",
        description = """
            사용자가 마이페이지에서 비밀번호를 변경합니다.
            
            변경 규칙:
            - 현재 비밀번호가 일치해야 함
            - 이전에 사용하던 비밀번호와 동일할 수 없음
            - 새 비밀번호는 암호화되어 저장됨
            
            비밀번호 변경 시 기존 Refresh/Access Token은 유지됩니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
        @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 / 새 비밀번호가 기존과 동일함"),
        @ApiResponse(responseCode = "401", description = "로그인 필요"),
        @ApiResponse(responseCode = "404", description = "회원 조회 실패 (탈퇴 포함)")
    })
    @PatchMapping("/password")
    public ResponseEntity<CommonResponseDto<Void>> updatePassword(
        @AuthenticationPrincipal MemberDetails memberDetails,
        @Valid @RequestBody PasswordUpdateRequestDto updateRequestDto
    ) {
        String email = memberDetails.getMember().getEmail();

        memberService.updatePassword(email, updateRequestDto.getCurrentPassword(), updateRequestDto.getNewPassword());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    @Operation(
        summary = "회원 탈퇴",
        description = """
            사용자가 자신의 계정을 탈퇴 처리합니다.
            Soft Delete 방식으로 탈퇴되며, 데이터는 일정 기간 보관됩니다.
            
            탈퇴 조건:
            - 현재 비밀번호가 일치해야 함
            - 이미 탈퇴한 회원은 다시 탈퇴할 수 없음
            
            탈퇴 후 처리:
            - 해당 회원의 Refresh Token / Access Token이 즉시 삭제됨
            - 이후 로그인 불가
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
        @ApiResponse(responseCode = "400", description = "비밀번호 불일치"),
        @ApiResponse(responseCode = "401", description = "로그인 필요"),
        @ApiResponse(responseCode = "404", description = "회원 조회 실패"),
        @ApiResponse(responseCode = "409", description = "이미 탈퇴된 회원")
    })
    @DeleteMapping
    public ResponseEntity<CommonResponseDto<Void>> withdraw(
        @AuthenticationPrincipal MemberDetails memberDetails,
        @Valid @RequestBody MemberWithdrawalRequestDto withdrawalRequestDto
    ) {
        memberService.withdraw(
            memberDetails.getMember().getId(),
            withdrawalRequestDto.getCurrentPassword(),
            withdrawalRequestDto.getReason());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.DELETE_SUCCESS, null));
    }

    @PatchMapping
    @Operation(
        summary = "내 정보 수정",
        description = """
            마이페이지에서 닉네임, 전화번호를 선택적으로 수정합니다.
            성별/생년월일은 수정 불가합니다.
            
            선택 수정(PATCH)이므로 하나만 변경하거나 둘 다 변경할 수 있습니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "내 정보 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonResponseDto.class)
            )),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 값"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "회원 조회 실패"),
        @ApiResponse(responseCode = "409", description = "닉네임 또는 전화번호 중복")
    })
    public ResponseEntity<CommonResponseDto<MemberInfoResponseDto>> updateMyInfo(
        @AuthenticationPrincipal MemberDetails memberDetails,
        @Valid @RequestBody MemberUpdateRequestDto memberUpdateRequestDto
    ) {
        Long memberId = memberDetails.getMember().getId();
        MemberInfoResponseDto memberInfoResponseDto = memberService.updateMyInfo(memberId, memberUpdateRequestDto);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, memberInfoResponseDto));
    }

    @GetMapping
    @Operation(
        summary = "내 정보 조회",
        description = """
            로그인한 사용자의 기본 정보를 조회합니다.
            이메일, 닉네임, 전화번호, 생년월일, 성별, 회원 유형(ROLE)을 반환합니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "내 정보 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonResponseDto.class)
            )),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "회원 조회 실패 (DELETED 포함)")
    })
    public ResponseEntity<CommonResponseDto<MemberInfoResponseDto>> getMyInfo(
        @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getMember().getId();
        MemberInfoResponseDto memberInfoResponseDto = memberService.getMyInfo(memberId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, memberInfoResponseDto));
    }

}
