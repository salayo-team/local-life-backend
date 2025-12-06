package com.salayo.locallifebackend.domain.magazine.controller;

import com.salayo.locallifebackend.domain.magazine.dto.MagazineListResponseDto;
import com.salayo.locallifebackend.domain.magazine.service.MagazineService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.dto.PaginationResponseDto;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

}
