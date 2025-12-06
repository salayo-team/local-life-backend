package com.salayo.locallifebackend.domain.localcreator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorDetailResponseDto;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorUpdateRequestDto;
import com.salayo.locallifebackend.domain.localcreator.service.LocalCreatorService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
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
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/localcreator/me")
@Tag(name = "LocalCreator", description = "로컬 크리에이터 회원 개인 정보 관련 API")
public class LocalCreatorController {

    private final LocalCreatorService localCreatorService;

    public LocalCreatorController(LocalCreatorService localCreatorService) {
        this.localCreatorService = localCreatorService;
    }

    @GetMapping
    @Operation(
        summary = "로컬 크리에이터 정보 조회",
        description = """
            로그인한 로컬 크리에이터의 기본 정보를 조회합니다.
            이메일, 사업자명, 주소, 전화번호, 생년월일, 증빙자료 presigned URL을 반환합니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "로컬 크리에이터 조회 실패")
    })
    public ResponseEntity<CommonResponseDto<LocalCreatorDetailResponseDto>> getMyCreatorInfo(
        @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getMember().getId();
        LocalCreatorDetailResponseDto localCreatorDetailResponseDto = localCreatorService.getMyCreatorInfo(memberId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, localCreatorDetailResponseDto));
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "로컬 크리에이터 정보 수정 (+ 재승인 요청)",
        description = """
            마이페이지에서 로컬 크리에이터의 상호명, 사업장 주소, 증빙자료를 수정합니다.
            수정 시 로컬 크리에이터 상태는 PENDING(재승인 대기)으로 변경되며,
            관리자의 재승인 이후부터 다시 체험 등록 등의 기능을 사용할 수 있습니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 및 재승인 요청 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 값"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "로컬 크리에이터 조회 실패"),
        @ApiResponse(responseCode = "409", description = "필수 증빙자료 누락 등으로 인한 충돌")
    })
    public ResponseEntity<CommonResponseDto<LocalCreatorDetailResponseDto>> updateMyCreatorInfo(
        @AuthenticationPrincipal MemberDetails memberDetails,
        @RequestPart("data") String requestDtoString,
        @RequestPart("file") List<MultipartFile> files,
        @RequestParam("filePurposes") List<String> filePurposesStrings
    ) {
        Long memberId = memberDetails.getMember().getId();

        ObjectMapper objectMapper = new ObjectMapper();

        LocalCreatorUpdateRequestDto localCreatorUpdateRequestDto;
        try {
            localCreatorUpdateRequestDto = objectMapper.readValue(requestDtoString, LocalCreatorUpdateRequestDto.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }

        List<FilePurpose> filePurposes = filePurposesStrings.stream()
            .map(FilePurpose::valueOf)
            .collect(Collectors.toList());

        LocalCreatorDetailResponseDto localCreatorDetailResponseDto = localCreatorService.updateMyCreatorInfo(memberId,
            localCreatorUpdateRequestDto, files, filePurposes);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, localCreatorDetailResponseDto));
    }

}
