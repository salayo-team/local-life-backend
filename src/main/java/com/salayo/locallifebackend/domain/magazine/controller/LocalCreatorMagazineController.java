package com.salayo.locallifebackend.domain.magazine.controller;

import com.salayo.locallifebackend.domain.magazine.service.MagazineService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/magazines")
@Tag(name = "Magazine | LocalCreator", description = "로컬 크리에이터 매거진 관리 API")
@PreAuthorize("hasRole('LOCAL_CREATOR')")
public class LocalCreatorMagazineController {

    private final MagazineService magazineService;

    public LocalCreatorMagazineController(MagazineService magazineService) {
        this.magazineService = magazineService;
    }

    @Operation(
        summary = "매거진 미리보기 토큰 검증",
        description = """
            매거진 초안/수정안/최종본 미리보기 링크를 검증합니다.

            사용 목적  
            - 로컬 크리에이터 본인만 해당 매거진 미리보기 접근 가능하도록 제한  
            - 미리보기 URL은 previewToken을 포함하며, 유효기간이 있습니다.

            검증 실패 상황  
            - 토큰이 존재하지 않거나 만료됨  
            - 토큰이 해당 로컬 크리에이터에게 발급된 것이 아님  
            - 토큰이 위조되었거나 형식이 잘못된 경우  
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 검증 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 토큰 형식 또는 위조된 토큰"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)"),
        @ApiResponse(responseCode = "403", description = "해당 미리보기 권한 없음"),
        @ApiResponse(responseCode = "404", description = "유효하지 않거나 만료된 토큰")
    })
    @PostMapping("/preview/validate")
    public ResponseEntity<CommonResponseDto<Void>> validatePreviewToken(
        @RequestParam String token,
        @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        magazineService.validatePreviewToken(token, memberDetails.getUsername());
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.VERIFICATION_SUCCESS, null));
    }

}
