package com.salayo.locallifebackend.domain.magazine.controller;

import com.salayo.locallifebackend.domain.magazine.dto.MagazineCreateRequestDto;
import com.salayo.locallifebackend.domain.magazine.service.MagazineService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/magazines")
@Tag(name = "Admin Magazine", description = "관리자 매거진 관리 API")
public class AdminMagazineController {

    private final MagazineService magazineService;

    public AdminMagazineController(MagazineService magazineService) {
        this.magazineService = magazineService;
    }

    @Operation(summary = "매거진 임시 저장 - 로컬 크리에이터 확인용", description = "관리자가 로컬 크리에이터를 인터뷰한 매거진을 임시 상태로 저장")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CommonResponseDto<Void>> createMagazine(
        @RequestBody @Valid MagazineCreateRequestDto createRequestDto,
        @AuthenticationPrincipal MemberDetails memberDetails
        ) {
        magazineService.createMagazine(createRequestDto, memberDetails.getMember().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, null));
    }
}
