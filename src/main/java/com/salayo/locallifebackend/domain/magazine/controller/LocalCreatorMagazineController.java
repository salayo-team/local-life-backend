package com.salayo.locallifebackend.domain.magazine.controller;

import com.salayo.locallifebackend.domain.magazine.service.MagazineService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/localcreator/magazines")
@Tag(name = "Magazine | LocalCreator", description = "로컬 크리에이터 매거진 관리 API")
public class LocalCreatorMagazineController {

    private final MagazineService magazineService;

    public LocalCreatorMagazineController(MagazineService magazineService) {
        this.magazineService = magazineService;
    }

    @Operation(summary = "매거진 미리보기 토큰 검증", description = "초안/수정안/최종안 미리보기 링크 접근 시, 해당 로컬 크리에이터 본인만 접근 가능하도록 검증")
    @PreAuthorize("hasRole('LOCAL_CREATOR')")
    @PostMapping("/preview/validate")
    public ResponseEntity<CommonResponseDto<Void>> validatePreviewToken(
        @RequestParam String token,
        @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        magazineService.validatePreviewToken(token, memberDetails.getUsername());
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.VERIFICATION_SUCCESS, null));
    }

}
