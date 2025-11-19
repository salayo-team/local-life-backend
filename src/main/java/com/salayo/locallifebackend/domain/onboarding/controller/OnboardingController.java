package com.salayo.locallifebackend.domain.onboarding.controller;

import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingAptitudeCheckRequestDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingProgressResponseDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingRegionRequestDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingStatusResponseDto;
import com.salayo.locallifebackend.domain.onboarding.enums.RegionType;
import com.salayo.locallifebackend.domain.onboarding.service.OnboardingService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/onboarding")
@PreAuthorize("hasRole('USER')")
@Tag(name = "OnBoarding", description = "온보딩 관련 API")
public class OnboardingController {

    private final OnboardingService onboardingService;

	public OnboardingController(OnboardingService onboardingService) {
		this.onboardingService = onboardingService;
	}

    @PostMapping("/start")
    @Operation(summary = "온보딩 시작", description = "회원 온보딩 프로세스를 시작합니다")
    public ResponseEntity<CommonResponseDto<OnboardingProgressResponseDto>> startOnboarding(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

        Long memberId = memberDetails.getMember().getId();
        log.info("온보딩 시작 요청 - memberId: {}", memberId);
        OnboardingProgressResponseDto progressResponseDto = onboardingService.startOnboarding(memberId);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, progressResponseDto)); // "온보딩이 시작되었습니다"
    }

    @PostMapping("/region")
    @Operation(summary = "선호 지역 특징 선택", description = "온보딩 시 선호 지역 특징(도시형/균형형/자연형)을 선택합니다")
    public ResponseEntity<CommonResponseDto<OnboardingProgressResponseDto>> selectRegion(
        @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
        @Valid @RequestBody OnboardingRegionRequestDto regionRequestDto) {

        Long memberId = memberDetails.getMember().getId();
        log.info("선호 지역 특징 선택 요청 - memberId: {}, regionType: {}", memberId, regionRequestDto.regionType());
        OnboardingProgressResponseDto progressResponseDto = onboardingService.selectRegion(memberId, regionRequestDto);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, progressResponseDto)); // "선호 지역이 설정되었습니다"
    }

    @PostMapping("/aptitude-check")
    @Operation(summary = "적성 인지 여부 확인",
        description = "적성을 알고 있는지 확인하고 수동 선택/AI 적성 검사로 분기합니다")
    public ResponseEntity<CommonResponseDto<OnboardingProgressResponseDto>> checkAptitudeKnowledge(
        @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
        @Valid @RequestBody OnboardingAptitudeCheckRequestDto aptitudeCheckRequestDto) {

        Long memberId = memberDetails.getMember().getId();
        log.info("적성 인지 여부 확인 - memberId: {}, knows: {}", memberId, aptitudeCheckRequestDto.knowsAptitude());
        OnboardingProgressResponseDto onboardingProgressResponseDto = onboardingService.checkAptitudeKnowledge(memberId, aptitudeCheckRequestDto);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, onboardingProgressResponseDto));
    }

    @PostMapping("/complete")
    @Operation(summary = "온보딩 완료", description = "적성 설정 완료 후 온보딩을 완료합니다")
    public ResponseEntity<CommonResponseDto<OnboardingProgressResponseDto>> completeOnboarding(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

        Long memberId = memberDetails.getMember().getId();
        log.info("온보딩 완료 요청 - memberId: {}", memberId);
        OnboardingProgressResponseDto progressResponseDto = onboardingService.completeOnboarding(memberId);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, progressResponseDto)); // "온보딩이 완료되었습니다"
    }
    @GetMapping("/status")
    @Operation(summary = "온보딩 진행 상태 조회(이어하기용)", description = "완료되지 않은 온보딩이 있는지 확인하고, 있다면 현재 단계를 반환합니다")
    public ResponseEntity<CommonResponseDto<OnboardingStatusResponseDto>> getOnboardingStatus(
        @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

        Long memberId = memberDetails.getMember().getId();
        OnboardingStatusResponseDto statusResponseDto = onboardingService.getOnboardingStatus(memberId);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, statusResponseDto));
    }

    @GetMapping("/progress")
    @Operation(summary = "온보딩 진행 상태 조회", description = "현재 온보딩 진행 상태를 조회합니다")
    public ResponseEntity<CommonResponseDto<OnboardingProgressResponseDto>> getCurrentProgress(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

        Long memberId = memberDetails.getMember().getId();
        log.info("온보딩 진행 상태 조회 - memberId: {}", memberId);
        OnboardingProgressResponseDto progressResponseDto = onboardingService.getCurrentProgress(memberId);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, progressResponseDto)); // "온보딩 진행 상태 조회 성공"
    }

    /**
     * 사용자의 선호 지역 목록 조회
     * 마이페이지에서 현재 선택된 지역들을 확인할 때 사용
     */
    @GetMapping("/regions")
    @Operation(summary = "내 선호 지역 목록 조회",
        description = "현재 사용자의 선호 지역 목록을 조회합니다 (마이페이지용)")
    public ResponseEntity<CommonResponseDto<List<String>>> getUserPreferredRegions(
        @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

        Long memberId = memberDetails.getMember().getId();
        log.info("선호 지역 목록 조회 요청 - memberId: {}", memberId);

        List<String> regions = onboardingService.getUserPreferredRegions(memberId);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, regions));
    }

    /**
     * 선호 지역 특징 변경 (재선택)
     * 마이페이지에서 선호 지역을 변경할 때 사용
     */
    @PutMapping("/regions")
    @Operation(summary = "선호 지역 변경",
        description = "선호 지역 특징을 변경합니다 (기존 지역 삭제 후 새로운 지역으로 자동 매핑)")
    public ResponseEntity<CommonResponseDto<List<String>>> updateUserPreferredRegions(
        @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
        @Valid @RequestBody OnboardingRegionRequestDto regionRequestDto) {

        Long memberId = memberDetails.getMember().getId();
        RegionType newRegionType = regionRequestDto.regionType();
        log.info("선호 지역 변경 요청 - memberId: {}, newRegionType: {}", memberId, newRegionType);

        List<String> updatedRegions = onboardingService.updateUserPreferredRegions(memberId, newRegionType);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, updatedRegions));
    }
}
