package com.salayo.locallifebackend.domain.admin.controller;

import com.salayo.locallifebackend.domain.admin.dto.CreatorPendingResponseDto;
import com.salayo.locallifebackend.domain.admin.service.AdminService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "관리자 관련 API")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "승인 대기 로컬 크리에이터 목록 조회", description = "관리자가 승인 대기 중인 로컬 크리에이터의 목록을 볼 수 있습니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/localcreators")
    public ResponseEntity<CommonResponseDto<List<CreatorPendingResponseDto>>> getPendingCreators() {
        List<CreatorPendingResponseDto> creators = adminService.getPendingCreators();

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, creators));
    }

    @Operation(summary = "로컬크리에이터 회원가입 승인", description = "관리자가 로컬 크리에이터의 회원가입을 승인합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/localcreators/{localcreatorId}/approve")
    public ResponseEntity<CommonResponseDto<Void>> approveCreator(@PathVariable Long localcreatorId) {
        adminService.approveCreator(localcreatorId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }
}
