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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
		description = """
			- 로컬 크리에이터가 체험 프로그램을 생성합니다.
			- LOCAL CREATOR ROLE만 생성 가능합니다.
			- 시작일 : 오늘 기준 7일 이후 ~ 1개월 이내, 종료일 : 시작일 기준 1주일 이후 ~ 3개월 이내
			- 기존에 생성한 체험 프로그램의 스케줄과 중복된 날짜&시간에는 생성 불가능합니다.
			- multipart/form-data 형식으로 요청하며, 썸네일 이미지 1개, 상세 이미지 최대 9개 총 10개의 이미지를 함께 업로드할 수 있습니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "체험 프로그램 생성 성공",
			content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청값 또는 검증 실패"),
		@ApiResponse(responseCode = "403", description = "접근 권한 없음"),
		@ApiResponse(responseCode = "404", description = "연관 리소스가 존재하지 않음 (회원, 적성 지역)"),
		@ApiResponse(responseCode = "409", description = "기존 체험 프로그램 스케줄과 시간이 중복")
	})
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
		description = """
			- 멤버가 체험 프로그램을 검색합니다.
			- 조건 & 정렬 설정, 제목 & 사업자명 검색 가능
			- USER ROLE만 검색 가능합니다.
			- 이모티콘, 특수문자, 공백은 제거 됩니다.
			- 기본값 : LATEST(최신순), PAGE(0), SIZE(10)
			- SoftDelete(DELETED) 체험 프로그램은 노출되지 않습니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "체험 프로그램 검색 성공",
			content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청값 또는 검증 실패"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
		@ApiResponse(responseCode = "403", description = "접근 권한 없음")
	})
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
		description = """
			- 로컬 크리에이터가 체험 프로그램을 삭제합니다.
			- LOCAL CREATOR ROLE만 삭제 가능합니다.
			- 프로그램 시작일 기준 7일 전인 경우 삭제할 수 없습니다.
			- 활성 상태의 예약이 있으면 삭제할 수 없습니다. (REJECTED, CANCELED, EXPIRED 상태만 가능)
			- SoftDelete(DELETED)로 상태 변경
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "체험 프로그램 삭제 성공",
			content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청값 또는 검증 실패"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
		@ApiResponse(responseCode = "403", description = "접근 권한 없음"),
		@ApiResponse(responseCode = "404", description = "해당 체험 프로그램을 찾을 수 없습니다.")
	})
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
	 */
	@Operation(
		summary = "로컬 크리에이터 체험 프로그램 전체 목록 조회 API",
		description = """
			- 로컬 크리에이터가 본인이 생성한 체험 프로그램 목록을 전체 조회합니다.
			-LOCAL CREATOR ROLE만 검색 가능합니다.
			- 기본값 : LATEST(최신순), PAGE(0), SIZE(10)
			- SoftDelete(DELETED) 체험 프로그램은 노출되지 않습니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "체험 프로그램 전체 목록 조회 성공",
			content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
		@ApiResponse(responseCode = "403", description = "접근 권한 없음"),
		@ApiResponse(responseCode = "404", description = "회원을 찾을 수 없습니다.")
	})
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
		description = """
			- 멤버가 체험 프로그램을 단건 상세 조회합니다.
			- USER ROLE만 검색 가능합니다.
			- SoftDelete(DELETED) 체험 프로그램은 노출되지 않습니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "체험 프로그램 전체 목록 조회 성공",
			content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
		@ApiResponse(responseCode = "400", description = "조회 불가능한 상태"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
		@ApiResponse(responseCode = "404", description = "해당 체험 프로그램을 찾을 수 없습니다.")
	})
	@PreAuthorize("hasRole('USER')")
	@GetMapping(value = "/programs/{programId}")
	public ResponseEntity<CommonResponseDto<ProgramDetailResponseDto>> getProgramDetail(
		@PathVariable("programId") Long programId) {

		ProgramDetailResponseDto responseDto = programService.getProgramDetail(programId);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responseDto));
	}


}
