package com.salayo.locallifebackend.domain.ai.aptitude.controller;

import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeAnswerRequestDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeResumeResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeSelectRequestDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestHistoryResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestResultResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestStartResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTextProgressResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.CanRetakeTestResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.service.AptitudeService;
import com.salayo.locallifebackend.domain.ai.aptitude.service.AptitudeTestHistoryService;
import com.salayo.locallifebackend.domain.ai.aptitude.service.AptitudeTestProgressService;
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
import java.util.List;
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
@RequestMapping("/ai/aptitude")
@PreAuthorize("hasRole('USER')")
@Tag(name = "Aptitude", description = "AI 기반 적성 검사 관련 API")
public class AptitudeController {

	private final AptitudeService aptitudeService;
	private final AptitudeTestProgressService aptitudeTestProgressService;
	private final AptitudeTestHistoryService testHistoryService;

	public AptitudeController(AptitudeService aptitudeService, AptitudeTestProgressService aptitudeTestProgressService,
		AptitudeTestHistoryService testHistoryService) {
		this.aptitudeService = aptitudeService;
		this.aptitudeTestProgressService = aptitudeTestProgressService;
		this.testHistoryService = testHistoryService;
	}

	@PostMapping("/test/start")
	@Operation(
		summary = "AI 적성 검사 시작",
		description = "AI 기반 적성 검사를 시작합니다. 온보딩 시 첫 검사는 제한 없이 가능하며, 마이페이지에서는 최대 5회까지 가능합니다. 기존 진행 중인 테스트는 초기화됩니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "AI 적성 검사 시작 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "검사 가능 횟수를 초과했거나 온보딩이 완료되지 않는 등 요청 값이 올바르지 않은 경우"
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
			description = "회원 또는 온보딩 정보가 존재하지 않는 경우"
		),
		@ApiResponse(
			responseCode = "408",
			description = "AI 응답 시간이 초과된 경우"
		),
		@ApiResponse(
			responseCode = "422",
			description = "AI 응답 형식이 올바르지 않아 처리할 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류 또는 AI 처리 중 오류가 발생한 경우"
		)
	})
	public CommonResponseDto<AptitudeTestStartResponseDto> startTest(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		log.info("적성 검사 시작 - memberId : {}", memberId);

		AptitudeTestStartResponseDto response = aptitudeTestProgressService.startTest(memberId);

		return CommonResponseDto.success(SuccessCode.AI_APTITUDE_TEST_START_SUCCESS, response);
	}

	@PostMapping("/test/answer")
	@Operation(
		summary = "AI 적성 검사 답변 제출",
		description = "AI 적성 검사 질문에 대한 답변을 제출하고 다음 질문을 받거나 최종 결과를 받습니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "AI로 답변 제출 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "요청 값이 유효하지 않거나 온보딩/검사 단계가 올바르지 않은 경우"
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
			description = "진행 중인 테스트가 없거나 회원 정보를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "408",
			description = "AI 응답 시간이 초과된 경우"
		),
		@ApiResponse(
			responseCode = "422",
			description = "AI 응답 형식이 올바르지 않아 처리할 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류 또는 AI 처리 중 오류가 발생한 경우"
		)
	})
	public CommonResponseDto<AptitudeTextProgressResponseDto> submitAnswer(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
		@Valid @RequestBody AptitudeAnswerRequestDto aptitudeAnswerRequestDto) {

		Long memberId = memberDetails.getMember().getId();
		log.info("답변 제출 - memberId: {}, step: {}", memberId, aptitudeAnswerRequestDto.getStep());

		AptitudeTextProgressResponseDto response = aptitudeService.submitAnswer(memberId, aptitudeAnswerRequestDto);

		return CommonResponseDto.success(SuccessCode.SUBMIT_ANSWER_SUCCESS, response);
	}

	@PostMapping("/select")
	@Operation(
		summary = "적성 수동 선택",
		description = "AI 추천 없이 사용자가 직접 적성을 선택합니다. 온보딩 시 '적성을 안다'를 선택한 경우 사용됩니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "적성 수동 선택 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 적성 타입이거나 온보딩/검사 단계가 올바르지 않은 경우"
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
			description = "회원 또는 적성 정보를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류가 발생한 경우"
		)
	})
	public CommonResponseDto<AptitudeTestResultResponseDto> selectManually(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
		@Valid @RequestBody AptitudeSelectRequestDto aptitudeSelectRequestDto) {

		Long memberId = memberDetails.getMember().getId();
		log.info("수동 선택 - memberId: {}, aptitude: {}", memberId, aptitudeSelectRequestDto.aptitudeType());

		AptitudeTestResultResponseDto response = aptitudeService.selectAptitudeManually(memberId, aptitudeSelectRequestDto.aptitudeType());

		return CommonResponseDto.success(SuccessCode.SELECT_SUCCESS, response);
	}

	@GetMapping("/can-retake")
	@Operation(
		summary = "AI 적성 재검사 가능 여부 확인",
		description = "AI 적성 재검사 가능 여부와 현재 검사 횟수를 확인합니다. 온보딩 완료 후 마이페이지에서 최대 5회까지 재검사 가능합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "AI 적성 재검사 가능 여부 조회 성공",
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
	public CommonResponseDto<CanRetakeTestResponseDto> canRetakeTest(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		CanRetakeTestResponseDto response = aptitudeTestProgressService.canRetakeTest(memberId);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, response);
	}

	@GetMapping("/history")
	@Operation(
		summary = "완료된 AI 적성 검사 이력 조회",
		description = "사용자의 완료된 AI 적성 검사 이력을 조회합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "완료된 AI 적성 검사 이력 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "요청 값이 올바르지 않은 경우"
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
	public CommonResponseDto<List<AptitudeTestHistoryResponseDto>> getCompletedHistory(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		List<AptitudeTestHistoryResponseDto> response = testHistoryService.getCompletedTestHistory(memberId);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, response);
	}

	@GetMapping("/history/session")
	@Operation(
		summary = "특정 세션의 AI 적성 검사 이력 조회",
		description = "특정 세션 ID에 해당하는 AI 적성 검사 이력을 조회합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "특정 세션 ID에 해당하는 AI 적성 검사 이력 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "세션 ID 형식이 올바르지 않거나 요청 값이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증이 필요하거나 토큰이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "403",
			description = "다른 사용자의 이력에 접근하려 할 때와 같이 권한이 없는 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "회원 또는 세션 ID에 해당하는 적성 검사 이력을 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류가 발생한 경우"
		)
	})
	public CommonResponseDto<List<AptitudeTestHistoryResponseDto>> getHistoryBySession(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
		@Parameter(description = "조회할 세션 ID", required = true, example = "APT-1-1234567890")
		@RequestParam String sessionId) {

		Long memberId = memberDetails.getMember().getId();
		List<AptitudeTestHistoryResponseDto> response = testHistoryService.getTestHistoryBySession(memberId, sessionId);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, response);
	}

	@GetMapping("/resume")
	@Operation(
		summary = "중단된 AI 적성 검사 이어하기",
		description = "이전에 중단된 AI 적성 검사를 이어서 진행합니다. 3단계 이상 진행한 테스트만 이어하기가 가능합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "AI 적성 검사 이어하기 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = AptitudeResumeResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "온보딩이 완료되지 않았거나 이어하기 조건에 맞지 않는 등 요청이 올바르지 않은 경우"
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
			description = "이어할 수 있는 테스트가 없거나 회원 정보를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "408",
			description = "AI 응답 시간이 초과된 경우"
		),
		@ApiResponse(
			responseCode = "422",
			description = "AI 응답 형식이 올바르지 않아 처리할 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류 또는 AI 처리 중 오류가 발생한 경우"
		)
	})
	public CommonResponseDto<AptitudeResumeResponseDto> resumeTest(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		AptitudeResumeResponseDto response = aptitudeTestProgressService.resumeTest(memberId);

		return CommonResponseDto.success(SuccessCode.RESUME_SUCCESS, response);
	}
}
