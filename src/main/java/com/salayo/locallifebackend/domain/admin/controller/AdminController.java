package com.salayo.locallifebackend.domain.admin.controller;

import com.salayo.locallifebackend.domain.admin.dto.CreatorPendingResponseDto;
import com.salayo.locallifebackend.domain.admin.dto.RejectReasonRequestDto;
import com.salayo.locallifebackend.domain.admin.service.AdminService;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorDetailResponseDto;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorEmailSearchResponseDto;
import com.salayo.locallifebackend.domain.localcreator.service.LocalCreatorService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "관리자 관련 API")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final LocalCreatorService localCreatorService;

    public AdminController(AdminService adminService, LocalCreatorService localCreatorService) {
        this.adminService = adminService;
        this.localCreatorService = localCreatorService;
    }

    @Operation(
        summary = "승인 대기 로컬 크리에이터 목록 조회",
        description = """
            관리자가 승인 대기 중인 로컬 크리에이터 목록을 조회합니다.
            승인 상태(PENDING)인 신청자만 반환되며,
            각 항목은 기본 정보와 제출 서류 상태를 포함합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = CreatorPendingResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인이 필요함)"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 접근 가능)"),
    })
    @GetMapping("/localcreators")
    public ResponseEntity<CommonResponseDto<List<CreatorPendingResponseDto>>> getPendingCreators() {
        List<CreatorPendingResponseDto> creators = adminService.getPendingCreators();

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, creators));
    }

    @Operation(
        summary = "로컬 크리에이터 요청 승인",
        description = """
            관리자가 로컬 크리에이터의 요청을 승인합니다.
            
            이 API는 다음 두 경우에 사용됩니다:
            - 로컬 크리에이터의 최초 신청 요청 승인
            - 정보 수정 후 재검토 요청 승인
            
            승인 가능한 상태는 'PENDING'뿐이며,
            이미 승인 또는 거절된 요청은 다시 승인할 수 없습니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "승인 성공"),
        @ApiResponse(responseCode = "400", description = "이미 처리된 신청자"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "로컬 크리에이터를 찾을 수 없음")
    })
    @PatchMapping("/localcreators/{localcreatorId}/approve")
    public ResponseEntity<CommonResponseDto<Void>> approveCreator(@PathVariable Long localcreatorId) {
        adminService.approveCreator(localcreatorId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    @Operation(
        summary = "로컬 크리에이터 요청 거절",
        description = """
            관리자가 로컬 크리에이터의 요청을 거절합니다.
            
            이 API는 다음 두 경우에 사용됩니다:
            - 최초 신청 요청 거절
            - 정보 수정 후 재검토 요청 최종 거절
            
            거절 사유(rejectReason)는 필수이며,
            거절 시 다음 작업이 자동으로 수행됩니다:
            - 요청 상태가 'REJECTED'로 변경됨
            - 재제출 링크가 포함된 안내 메일 발송
            - 재제출 토큰은 72시간 동안 유효
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "거절 처리 성공"),
        @ApiResponse(responseCode = "400", description = "이미 처리된 신청자 / 잘못된 입력값"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "로컬 크리에이터를 찾을 수 없음")
    })
    @PatchMapping("/localcreators/{localcreatorId}/reject")
    public ResponseEntity<CommonResponseDto<Void>> rejectCreator(
        @PathVariable Long localcreatorId,
        @RequestBody @Valid RejectReasonRequestDto requestDto) {
        adminService.rejectCreator(localcreatorId, requestDto.getRejectReason());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    @Operation(
        summary = "로컬 크리에이터 신청 상세 조회",
        description = """
            관리자가 로컬 크리에이터 회원가입 신청자의 상세 정보를 조회합니다.
            반환값에는 기본 프로필 정보, 제출된 증빙 파일 목록,
            각 파일에 대한 presigned URL이 포함됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
        @ApiResponse(responseCode = "404", description = "해당 신청자를 찾을 수 없음")
    })
    @GetMapping("/localcreators/{localcreatorId}")
    public ResponseEntity<CommonResponseDto<LocalCreatorDetailResponseDto>> getLocalCreatorDetail(@PathVariable Long localcreatorId) {
        LocalCreatorDetailResponseDto responseDto = localCreatorService.getLocalCreatorDetail(localcreatorId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responseDto));
    }

    @Operation(
        summary = "이메일로 로컬 크리에이터 정보 조회",
        description = """
            입력된 이메일을 가진 로컬 크리에이터의 기본 정보를 조회합니다.
            반환값에는 ID, 상호명, 이메일이 포함됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 이메일 형식"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "해당 이메일의 신청자를 찾을 수 없음")
    })
    @GetMapping("/localcreators/search")
    public ResponseEntity<CommonResponseDto<LocalCreatorEmailSearchResponseDto>> getLocalCreatorByEmail(@RequestParam String email) {
        LocalCreatorEmailSearchResponseDto emailSearchResponseDto = adminService.findLocalCreatorByEmail(email);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, emailSearchResponseDto));
    }
}
