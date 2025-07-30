package com.salayo.locallifebackend.domain.ai.aptitude.controller;

import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeAnswerRequestDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestResultResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestStartResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTextProgressResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.CanRetakeTestResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.ai.aptitude.service.AptitudeService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/ai/aptitude")
@RequiredArgsConstructor
@Tag(name = "Aptitude", description = "적성 검사 관련 API")
public class AptitudeController {

	private final AptitudeService aptitudeService;

	@PostMapping("/test/start")
	@PreAuthorize("hasRole('USER')")
	@Operation(
		summary = "적성 검사 시작",
		description = "AI 기반 적성 검사를 시작합니다. 최대 5회까지 가능합니다. 기존 진행 중인 테스트는 초기화됩니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "적성 검사 시작 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "적성 검사 횟수 초과 (APTITUDE_TEST_LIMIT_EXCEEDED)"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		),
		@ApiResponse(
			responseCode = "404",
			description = "회원을 찾을 수 없음 (MEMBER_NOT_FOUND)"
		)
	})
	public CommonResponseDto<AptitudeTestStartResponseDto> startTest(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {
		Long memberId = memberDetails.getMember().getId();
		log.info("적성 검사 시작 - memberId : {}", memberId);
		AptitudeTestStartResponseDto response = aptitudeService.startTest(memberId);
		return CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, response);
	}

	@PostMapping("/test/answer")
	@PreAuthorize("hasRole('USER')")
	@Operation(
		summary = "적성 검사 답변 제출",
		description = "적성 검사 질문에 대한 답변을 제출하고 다음 질문을 받거나 최종 결과를 받습니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "답변 제출 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		),
		@ApiResponse(
			responseCode = "404",
			description = "진행 중인 테스트가 없음 (NOT_FOUND_TEST_PROGRESS) 또는 회원을 찾을 수 없음 (MEMBER_NOT_FOUND)"
		)
	})
	public CommonResponseDto<AptitudeTextProgressResponseDto> submitAnswer(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
		@Valid @RequestBody AptitudeAnswerRequestDto aptitudeAnswerRequestDto) {
		Long memberId = memberDetails.getMember().getId();
		log.info("답변 제출 - memberId: {}, step: {}", memberId, aptitudeAnswerRequestDto.getStep());
		AptitudeTextProgressResponseDto response = aptitudeService.submitAnswer(memberId, aptitudeAnswerRequestDto);
		return CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, response);
	}

	@PostMapping("/select")
	@PreAuthorize("hasRole('USER')")
	@Operation(
		summary = "적성 수동 선택",
		description = "AI 추천 없이 직접 적성을 선택합니다. 온보딩 시 '적성을 안다'를 선택한 경우 사용됩니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "적성 선택 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 적성 타입"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		),
		@ApiResponse(
			responseCode = "404",
			description = "회원을 찾을 수 없음 (MEMBER_NOT_FOUND)"
		)
	})
	public CommonResponseDto<AptitudeTestResultResponseDto> selectManually(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
		@Parameter(
			description = "선택할 적성 타입",
			required = true,
			example = "NATURE",
			schema = @Schema(implementation = AptitudeType.class)
		)
		@RequestParam AptitudeType aptitudeType) {
		Long memberId = memberDetails.getMember().getId();
		log.info("수동 선택 - memberId: {}, aptitude: {}", memberId, aptitudeType);
		AptitudeTestResultResponseDto response = aptitudeService.selectAptitudeManually(memberId, aptitudeType);
		return CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, response);
	}

	@GetMapping("/can-retake")
	@PreAuthorize("hasRole('USER')")
	@Operation(
		summary = "재검사 가능 여부 확인",
		description = "적성 재검사 가능 여부와 현재 검사 횟수를 확인합니다. 마이페이지에서 사용됩니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		),
		@ApiResponse(
			responseCode = "404",
			description = "회원을 찾을 수 없음 (MEMBER_NOT_FOUND)"
		)
	})
	public CommonResponseDto<CanRetakeTestResponseDto> canRetakeTest(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {
		Long memberId = memberDetails.getMember().getId();
		CanRetakeTestResponseDto response = aptitudeService.canRetakeTest(memberId);
		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, response);
	}
}
