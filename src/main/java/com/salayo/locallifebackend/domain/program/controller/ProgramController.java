package com.salayo.locallifebackend.domain.program.controller;

import com.salayo.locallifebackend.domain.program.dto.ProgramCreateRequestDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateResponseDto;
import com.salayo.locallifebackend.domain.program.service.ProgramService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProgramController {

	private final ProgramService programService;

	public ProgramController(ProgramService programService) {
		this.programService = programService;
	}

	/**
	 * 체험 프로그램 생성 API
	 */
	@Operation(
		summary = "체험 프로그램 생성",
		description = "로컬 크리에이터 유저가 체험 프로그램을 생성합니다."
	)
	@PostMapping("/programs")
	public ResponseEntity<CommonResponseDto<ProgramCreateResponseDto>> createProgram(
		@RequestBody ProgramCreateRequestDto requestDto) {

		ProgramCreateResponseDto responseDto = programService.createProgram(1L, requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto));
	}
}
