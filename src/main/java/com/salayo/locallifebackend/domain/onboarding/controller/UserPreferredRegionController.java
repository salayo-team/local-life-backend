package com.salayo.locallifebackend.domain.onboarding.controller;

import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingRegionRequestDto;
import com.salayo.locallifebackend.domain.onboarding.service.UserPreferredRegionService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 사용자 선호 지역 관리 Controller
 * 마이페이지 및 선호 지역 조회/수정 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/preferred-regions")
@PreAuthorize("hasRole('USER')")
@Tag(name = "UserPreferredRegion", description = "사용자 선호 지역 관리 API")
public class UserPreferredRegionController {

    private final UserPreferredRegionService userPreferredRegionService;

    public UserPreferredRegionController(UserPreferredRegionService userPreferredRegionService) {
        this.userPreferredRegionService = userPreferredRegionService;
    }

    /**
     * 사용자의 선호 지역 목록 조회
     * 마이페이지에서 현재 선택된 지역들을 확인할 때 사용
     */
    @GetMapping("/view")
    @Operation(summary = "내 선호 지역 목록 조회",
        description = "현재 사용자의 선호 지역 목록을 조회합니다 (마이페이지용)")
    public ResponseEntity<CommonResponseDto<List<String>>> getUserPreferredRegions(
        @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

        Long memberId = memberDetails.getMember().getId();
        log.info("선호 지역 목록 조회 요청 - memberId: {}", memberId);

        List<String> regions = userPreferredRegionService.getUserPreferredRegions(memberId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, regions));
    }

    /**
     * 선호 지역 특징 변경 (재선택)
     * 마이페이지에서 선호 지역을 변경할 때 사용
     */
    @PutMapping("/reselect")
    @Operation(summary = "선호 지역 변경",
        description = "선호 지역 특징을 변경합니다 (기존 지역 삭제 후 새로운 지역으로 자동 매핑)")
    public ResponseEntity<CommonResponseDto<List<String>>> updateUserPreferredRegions(
        @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
        @Valid @RequestBody OnboardingRegionRequestDto regionRequestDto) {

        Long memberId = memberDetails.getMember().getId();
        log.info("선호 지역 변경 요청 - memberId: {}, newRegionType: {}", memberId, regionRequestDto.regionType());

        List<String> updatedRegions = userPreferredRegionService.updateUserPreferredRegions(
            memberId, regionRequestDto.regionType());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, updatedRegions));
    }
}
