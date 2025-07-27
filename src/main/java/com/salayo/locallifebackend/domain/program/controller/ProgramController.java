package com.salayo.locallifebackend.domain.program.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateRequestDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateResponseDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramSearchRequestDto;
import com.salayo.locallifebackend.domain.program.service.ProgramService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.dto.PaginationResponseDto;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	@PreAuthorize("hasRole('LOCAL_CREATOR')")
	@PostMapping(value = "/localcreator/programs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CommonResponseDto<ProgramCreateResponseDto>> createProgram(
		@RequestPart("data") String requestDtoString,
		@RequestPart("file") List<MultipartFile> files,
		@RequestParam("filePurposes") List<String> filePurposesStrings,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		ProgramCreateRequestDto requestDto;

		try {
			requestDto = objectMapper.readValue(requestDtoString, ProgramCreateRequestDto.class);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.INVALID_PARAMETER);
		}

		List<FilePurpose> filePurposes = filePurposesStrings.stream()
			.map(FilePurpose::valueOf)
			.collect(Collectors.toList());

		Long memberId = memberDetails.getMember().getId();
		ProgramCreateResponseDto responseDto = programService.createProgram(memberId, requestDto, files, filePurposes);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto));
	}

	/**
	 * 체험 프로그램 전체 조회 API
	 */
	@Operation(
		summary = "체험 프로그램 조회",
		description = "유저가 정렬 조건을 설정하여 체험 프로그램을 조회 할 수 있습니다.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@GetMapping(value = "/program/search")
	public ResponseEntity<CommonResponseDto<PaginationResponseDto<ProgramCreateResponseDto>>> searchProgram(
		@ModelAttribute ProgramSearchRequestDto requestDto,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();

		PaginationResponseDto<ProgramCreateResponseDto> responsePage = programService.searchProgram(requestDto, memberId);

		PaginationResponseDto<ProgramCreateResponseDto> pagination = new PaginationResponseDto<ProgramCreateResponseDto>(
			responsePage.getContent(),
			responsePage.getPage(),
			responsePage.getSize(),
			responsePage.getTotalElements(),
			responsePage.getTotalPages()
		);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, pagination));
	}

}
