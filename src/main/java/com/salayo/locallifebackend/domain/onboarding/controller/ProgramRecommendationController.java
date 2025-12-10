package com.salayo.locallifebackend.domain.onboarding.controller;

import com.salayo.locallifebackend.domain.onboarding.dto.RecommendedProgramResponseDto;
import com.salayo.locallifebackend.domain.onboarding.service.ProgramRecommendationService;
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
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 체험 프로그램 추천 Controller 온보딩 완료 후 사용자 맞춤 프로그램 6개 추천 조회 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/recommended-programs")
@PreAuthorize("hasRole('USER')")
@Tag(name = "OnBoarding", description = "체험 프로그램 추천 API")
public class ProgramRecommendationController {

	private final ProgramRecommendationService programRecommendationService;

	public ProgramRecommendationController(ProgramRecommendationService programRecommendationService) {
		this.programRecommendationService = programRecommendationService;
	}

	@GetMapping("/random")
	@Operation(
		summary = "체험 프로그램 6개 랜덤 추천",
		description = "온보딩에서 설정된 사용자 적성 타입과 선호 지역을 기반으로 조건에 맞는 체험 프로그램을 조회하고, 그 중 6개를 랜덤으로 추천합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "체험 프로그램 6개 랜덤 추천 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "필수 입력값이 누락되었거나 잘못된 요청 값인 경우"
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
			description = "회원 정보, 적성 정보 또는 카테고리 정보를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류가 발생한 경우"
		)
	})
	public ResponseEntity<CommonResponseDto<List<RecommendedProgramResponseDto>>> getRandomRecommendedPrograms(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		log.info("체험 프로그램 랜덤 추천 요청 - memberId: {}", memberId);

		List<RecommendedProgramResponseDto> recommendedPrograms = programRecommendationService.getRecommendedPrograms(memberId);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.RECOMMENDATION_SUCCESS, recommendedPrograms));
	}
}
