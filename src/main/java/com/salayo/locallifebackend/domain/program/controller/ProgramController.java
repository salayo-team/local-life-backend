package com.salayo.locallifebackend.domain.program.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateRequestDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateResponseDto;
import com.salayo.locallifebackend.domain.program.service.ProgramService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "Program", description = "체험 프로그램 관련 API")
@RestController
public class ProgramController {

	private final ProgramService programService;
	private final ObjectMapper objectMapper;

	public ProgramController(ProgramService programService, ObjectMapper objectMapper) {

		this.programService = programService;
		this.objectMapper = objectMapper;
	}

	/**
	 * 체험 프로그램 생성 API
	 */
	@Operation(
		summary = "체험 프로그램 생성",
		description = "로컬 크리에이터 유저가 체험 프로그램을 생성합니다.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@PostMapping(value = "/localcreator/programs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CommonResponseDto<ProgramCreateResponseDto>> createProgram(
		@RequestPart("data") String requestDtoString,
		@RequestPart("file") List<MultipartFile> files,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		ProgramCreateRequestDto requestDto;

		try {
			requestDto = objectMapper.readValue(requestDtoString, ProgramCreateRequestDto.class);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.INVALID_PARAMETER);
		}
		Long memberId = memberDetails.getMember().getId();
		ProgramCreateResponseDto responseDto = programService.createProgram(memberId, requestDto, files);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto));
	}
}
