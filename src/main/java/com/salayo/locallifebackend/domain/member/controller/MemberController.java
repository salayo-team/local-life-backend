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

    @Operation(summary = "비밀번호 변경 - 마이페이지", description = "현재 비밀번호를 확인한 뒤, 새 비밀번호로 변경합니다.")
    @PatchMapping("/password")
    public ResponseEntity<CommonResponseDto<Void>> updatePassword(
        @AuthenticationPrincipal MemberDetails memberDetails,
        @Valid @RequestBody PasswordUpdateRequestDto updateRequestDto
        ) {
        String email = memberDetails.getMember().getEmail();

        memberService.updatePassword(email, updateRequestDto.getCurrentPassword(), updateRequestDto.getNewPassword());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    @Operation(summary = "회원 탈퇴", description = "회원을 탈퇴 처리합니다.")
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

}
