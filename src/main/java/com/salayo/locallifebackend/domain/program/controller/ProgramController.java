package com.salayo.locallifebackend.domain.program.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateRequestDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateResponseDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramDetailResponseDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramListResponseDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramSearchRequestDto;
import com.salayo.locallifebackend.domain.program.service.ProgramService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.dto.PaginationResponseDto;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "Program", description = "체험 프로그램 도메인 API")
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
		summary = "체험 프로그램 생성 API",
		description = "로컬 크리에이터가 체험 프로그램을 생성합니다."
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
	 * 체험 프로그램 검색 API
	 */
	@Operation(
		summary = "체험 프로그램 검색 API",
		description = "멤버가 정렬 조건을 설정하여 체험 프로그램을 검색합니다. (조건 & 정렬 설정, 제목 & 사업자명 검색 가능)"
	)
	@PreAuthorize("hasRole('USER')")
	@GetMapping(value = "/programs/search")
	public ResponseEntity<CommonResponseDto<PaginationResponseDto<ProgramListResponseDto>>> searchProgram(
		@Valid @ModelAttribute ProgramSearchRequestDto requestDto) {

		PaginationResponseDto<ProgramListResponseDto> responsePage = programService.searchProgram(requestDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responsePage));
	}

	/**
	 * 체험 프로그램 삭제 API
	 */
	@Operation(
		summary = "체험 프로그램 삭제 API",
		description = "로컬 크리에이터가 체험 프로그램을 삭제합니다."
	)
	@PreAuthorize("hasRole('LOCAL_CREATOR')")
	@PatchMapping("/programs/{programId}")
	public ResponseEntity<CommonResponseDto<Long>> deleteProgram(
		@PathVariable("programId") Long programId,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		programService.deleteProgram(programId, memberId);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.DELETE_SUCCESS, programId));
	}

	/**
	 * 로컬 크리에이터 체험 프로그램 전체 조회 API
	 * - 로컬 크리에이터가 생성한 체험 프로그램 전체 조회
	 */
	@Operation(
		summary = "로컬 크리에이터 체험 프로그램 전체 조회 API",
		description = "로컬 크리에이터가 생성한 체험 프로그램을 전체 조회합니다."
	)
	@PreAuthorize("hasRole('LOCAL_CREATOR')")
	@GetMapping("/localcreator/programs")
	public ResponseEntity<CommonResponseDto<PaginationResponseDto<ProgramListResponseDto>>> getProgramForLocalCreator(
		@RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "10") int size,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		PaginationResponseDto<ProgramListResponseDto> responsePage = programService.getProgramForLocalCreator(memberId, page, size);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responsePage));
	}

	/**
	 * 체험 프로그램 단건 상세 조회 API
	 */
	@Operation(
		summary = "체험 프로그램 단건 상세 조회 API",
		description = "멤버가 체험 프로그램을 단건 상세 조회합니다."
	)
	@PreAuthorize("hasRole('USER')")
	@GetMapping(value = "/programs/{programId}")
	public ResponseEntity<CommonResponseDto<ProgramDetailResponseDto>> getProgramDetail(
		@PathVariable("programId") Long programId) {

		ProgramDetailResponseDto responseDto = programService.getProgramDetail(programId);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responseDto));
	}


}
