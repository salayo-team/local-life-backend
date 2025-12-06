package com.salayo.locallifebackend.domain.magazine.controller;

import com.salayo.locallifebackend.domain.magazine.dto.MagazineDetailResponseDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineListResponseDto;
import com.salayo.locallifebackend.domain.magazine.service.MagazineService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.dto.PaginationResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/magazines")
@Tag(name = "Magazine | Public", description = "매거진 공개 조회 API")
public class MagazineController {

    private final MagazineService magazineService;

    public MagazineController(MagazineService magazineService) {
        this.magazineService = magazineService;
    }

    @Operation(
        summary = "매거진 목록 조회",
        description = """
            REGISTERED 상태의 매거진 목록을 조회합니다.
            - USER, ADMIN만 접근 가능
            - LOCAL_CREATOR는 접근할 수 없습니다.
            - SoftDelete(DELETED) 매거진은 노출되지 않습니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/public")
    public ResponseEntity<CommonResponseDto<PaginationResponseDto<MagazineListResponseDto>>> getRegisteredMagazines(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PaginationResponseDto<MagazineListResponseDto> magazineListResponseDtoPaginationResponseDto =
            magazineService.getRegisteredMagazines(page, size);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, magazineListResponseDtoPaginationResponseDto));
    }

    @Operation(
        summary = "매거진 상세 조회",
        description = """
        REGISTERED 상태의 매거진 상세 정보를 조회합니다.
        - USER, ADMIN만 접근 가능
        - LOCAL_CREATOR는 접근할 수 없습니다.
        - 조회 시 views 증가
        """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "매거진 찾을 수 없음")
    })
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/public/{magazineId}")
    public ResponseEntity<CommonResponseDto<MagazineDetailResponseDto>> getRegisteredMagazineDetail(
        @PathVariable Long magazineId,
        @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        MagazineDetailResponseDto magazineDetailResponseDto =
            magazineService.getRegisteredMagazineDetail(magazineId, memberDetails.getMember());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, magazineDetailResponseDto));
    }


}
