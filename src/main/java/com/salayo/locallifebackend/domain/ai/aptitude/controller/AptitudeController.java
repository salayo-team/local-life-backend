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
@RequestMapping("/ai/aptitude")
@RequiredArgsConstructor
public class AptitudeController {

	private final AptitudeService aptitudeService;

	@PostMapping("/test/start")
	@PreAuthorize("hasRole('USER')")
	public CommonResponseDto<AptitudeTestStartResponseDto> startTest(
		@AuthenticationPrincipal MemberDetails memberDetails) {
		Long memberId = memberDetails.getMember().getId();
		log.info("적성 검사 시작 - memberId : {}", memberId);
		AptitudeTestStartResponseDto response = aptitudeService.startTest(memberId);
		return CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, response);
	}

	@PostMapping("/test/answer")
	@PreAuthorize("hasRole('USER')")
	public CommonResponseDto<AptitudeTextProgressResponseDto> submitAnswer(
		@AuthenticationPrincipal MemberDetails memberDetails,
		@Valid @RequestBody AptitudeAnswerRequestDto aptitudeAnswerRequestDto) {
		Long memberId = memberDetails.getMember().getId();
		log.info("답변 제출 - memberId: {}, step: {}", memberId, aptitudeAnswerRequestDto.getStep());
		AptitudeTextProgressResponseDto response = aptitudeService.submitAnswer(memberId, aptitudeAnswerRequestDto);
		return CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, response);
	}

	@PostMapping("/select")
	@PreAuthorize("hasRole('USER')")
	public CommonResponseDto<AptitudeTestResultResponseDto> selectManually(
		@AuthenticationPrincipal MemberDetails memberDetails,
		@RequestParam AptitudeType aptitudeType) {
		Long memberId = memberDetails.getMember().getId();
		log.info("수동 선택 - memberId: {}, aptitude: {}", memberId, aptitudeType);
		AptitudeTestResultResponseDto response = aptitudeService.selectAptitudeManually(memberId, aptitudeType);
		return CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, response);
	}

	@GetMapping("/can-retake")
	@PreAuthorize("hasRole('USER')")
	public CommonResponseDto<CanRetakeTestResponseDto> canRetakeTest(
		@AuthenticationPrincipal MemberDetails memberDetails) {
		Long memberId = memberDetails.getMember().getId();
		CanRetakeTestResponseDto response = aptitudeService.canRetakeTest(memberId);
		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, response);
	}
}
