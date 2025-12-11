package com.salayo.locallifebackend.domain.onboarding.controller;

import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingAptitudeCheckRequestDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingProgressResponseDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingRegionRequestDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingStatusResponseDto;
import com.salayo.locallifebackend.domain.onboarding.service.UserRegionFacadeService;
import com.salayo.locallifebackend.domain.onboarding.service.OnboardingService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
	private final UserRegionFacadeService userRegionFacadeService;

	public OnboardingController(OnboardingService onboardingService, UserRegionFacadeService userRegionFacadeService) {
		this.onboardingService = onboardingService;
		this.userRegionFacadeService = userRegionFacadeService;
	}

	@PostMapping("/start")
	@Operation(
		summary = "온보딩 시작",
		description = "회원 온보딩 프로세스를 시작합니다. 이미 온보딩이 완료된 회원은 완료 상태와 함께 최종 적성 정보를 반환합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "온보딩 시작 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증이 필요하거나 토큰이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "403",
			description = "접근 권한이 없거나 차단된 인증으로 요청한 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "회원 정보를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류가 발생한 경우"
		)
	})
	public ResponseEntity<CommonResponseDto<OnboardingProgressResponseDto>> startOnboarding(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		log.info("온보딩 시작 요청 - memberId: {}", memberId);

		OnboardingProgressResponseDto progressResponseDto = onboardingService.startOnboarding(memberId);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.ONBOARDING_START_SUCCESS, progressResponseDto));
	}

	@GetMapping("/progress")
	@Operation(
		summary = "온보딩 진행 상태 조회",
		description = "현재 온보딩 진행 상태를 조회합니다. 현재 단계, 다음 단계, 선택된 선호 지역 특징 및 적성 정보를 반환합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "온보딩 진행 상태 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증이 필요하거나 토큰이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "403",
			description = "접근 권한이 없거나 차단된 인증으로 요청한 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "온보딩이 시작되지 않았거나 회원 정보를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류가 발생한 경우"
		)
	})
	public ResponseEntity<CommonResponseDto<OnboardingProgressResponseDto>> getCurrentProgress(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		log.info("온보딩 진행 상태 조회 시작 - memberId: {}, ", memberId);

		OnboardingProgressResponseDto progressResponseDto = onboardingService.getCurrentProgress(memberId);
		log.info("온보딩 진행 상태 조회 결과: {}", progressResponseDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, progressResponseDto));
	}

	@GetMapping("/status")
	@Operation(
		summary = "온보딩 진행 상태 조회(이어하기용)",
		description = "완료되지 않은 온보딩이 있는지 확인하고, 있다면 현재 단계를 반환합니다. 온보딩 완료 시에는 선호 지역과 최종 적성 타입만 함께 반환합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "온보딩 상태 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증이 필요하거나 토큰이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "403",
			description = "접근 권한이 없거나 차단된 인증으로 요청한 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "회원 정보를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류가 발생한 경우"
		)
	})
	public ResponseEntity<CommonResponseDto<OnboardingStatusResponseDto>> getOnboardingStatus(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();

		OnboardingStatusResponseDto statusResponseDto = onboardingService.getOnboardingStatus(memberId);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, statusResponseDto));
	}

	@PostMapping("/region")
	@Operation(
		summary = "선호 지역 특징 선택",
		description = "온보딩 시 선호 지역 특징(도시형/균형형/자연형)을 선택합니다. 선택된 특징에 따라 선호 지역 목록이 자동 매핑됩니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "선호 지역 특징 선택 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "온보딩 단계가 올바르지 않거나 요청 값이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증이 필요하거나 토큰이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "403",
			description = "접근 권한이 없거나 차단된 인증으로 요청한 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "온보딩이 시작되지 않았거나 회원 정보를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류가 발생한 경우"
		)
	})
	public ResponseEntity<CommonResponseDto<OnboardingProgressResponseDto>> selectRegion(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
		@Valid @RequestBody OnboardingRegionRequestDto regionRequestDto) {

		Long memberId = memberDetails.getMember().getId();
		log.info("선호 지역 특징 선택 요청 - memberId: {}, regionType: {}", memberId, regionRequestDto.regionType());

		OnboardingProgressResponseDto progressResponseDto = userRegionFacadeService.coordinateSelectRegion(memberId, regionRequestDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.SELECT_SUCCESS, progressResponseDto));
	}

	@PostMapping("/aptitude-check")
	@Operation(
		summary = "적성 인지 여부 확인",
		description = "적성을 알고 있는지 확인하고, 수동 선택 또는 AI 적성 검사 단계로 분기합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "적성 인지 여부 확인 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "온보딩 단계가 올바르지 않거나 요청 값이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증이 필요하거나 토큰이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "403",
			description = "접근 권한이 없거나 차단된 인증으로 요청한 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "온보딩이 시작되지 않았거나 회원 정보를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류가 발생한 경우"
		)
	})
	public ResponseEntity<CommonResponseDto<OnboardingProgressResponseDto>> checkAptitudeKnowledge(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
		@Valid @RequestBody OnboardingAptitudeCheckRequestDto aptitudeCheckRequestDto) {

		Long memberId = memberDetails.getMember().getId();
		log.info("적성 인지 여부 확인 - memberId: {}, knows: {}", memberId, aptitudeCheckRequestDto.knowsAptitude());

		OnboardingProgressResponseDto onboardingProgressResponseDto = onboardingService.checkAptitudeKnowledge(memberId,
			aptitudeCheckRequestDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.SELECT_SUCCESS, onboardingProgressResponseDto));
	}

	@PostMapping("/complete")
	@Operation(
		summary = "온보딩 완료",
		description = "적성 설정 완료 후 온보딩을 최종 완료 처리합니다. 사용자 선호 지역, 최종 적성 타입과 함께 완료 상태를 반환합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "온보딩 완료 처리 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "온보딩 단계가 올바르지 않거나 완료 가능한 단계가 아닌 경우"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증이 필요하거나 토큰이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "403",
			description = "접근 권한이 없거나 차단된 인증으로 요청한 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "온보딩이 시작되지 않았거나 적성 정보 또는 회원 정보를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류가 발생한 경우"
		)
	})
	public ResponseEntity<CommonResponseDto<OnboardingProgressResponseDto>> completeOnboarding(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		log.info("온보딩 완료 요청 - memberId: {}", memberId);

		OnboardingProgressResponseDto progressResponseDto = onboardingService.completeOnboarding(memberId);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.ONBOARDING_COMPLETE_SUCCESS, progressResponseDto));
	}
}
