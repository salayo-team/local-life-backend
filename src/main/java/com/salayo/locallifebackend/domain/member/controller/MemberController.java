package com.salayo.locallifebackend.domain.member.controller;

import com.salayo.locallifebackend.domain.member.dto.PasswordUpdateRequestDto;
import com.salayo.locallifebackend.domain.member.service.MemberService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

}
