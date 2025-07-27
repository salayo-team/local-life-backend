package com.salayo.locallifebackend.domain.magazine.controller;

import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineCreateRequestDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineDraftListResponseDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineFileUploadResponseDto;
import com.salayo.locallifebackend.domain.magazine.service.MagazineFileService;
import com.salayo.locallifebackend.domain.magazine.service.MagazineService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/magazines")
@Tag(name = "Magazine | Admin", description = "관리자 매거진 관리 API")
public class AdminMagazineController {

    private final MagazineService magazineService;
    private final MagazineFileService magazineFileService;

    public AdminMagazineController(MagazineService magazineService, MagazineFileService magazineFileService) {
        this.magazineService = magazineService;
        this.magazineFileService = magazineFileService;
    }

    @Operation(summary = "매거진 임시 저장 - 로컬 크리에이터 확인용", description = "관리자가 로컬 크리에이터를 인터뷰한 매거진을 임시 상태로 저장")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CommonResponseDto<Void>> createMagazine(
        @RequestBody @Valid MagazineCreateRequestDto createRequestDto,
        @AuthenticationPrincipal MemberDetails memberDetails
        ) {
        magazineService.createMagazine(createRequestDto, memberDetails.getMember().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, null));
    }

    @Operation(summary = "매거진 파일 업로드", description = "매거진 썸네일/상세이미지 업로드(purpose=THUMBNAIL/DETAIL_IMAGE)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponseDto<List<MagazineFileUploadResponseDto>>> uploadFiles(
        @RequestPart("files") @NotNull List<MultipartFile> files,
        @RequestParam("purpose")FilePurpose filePurpose
    ) {
        List<MagazineFileUploadResponseDto> uploadResponseDto = magazineFileService.uploadFiles(files, filePurpose);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FILE_UPLOAD_SUCCESS, uploadResponseDto));
    }

    @Operation(summary = "1차등록(임시등록) 매거진 목록 조회", description = "관리자가 1차등록 매거진 목록을 조회")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<CommonResponseDto<List<MagazineDraftListResponseDto>>> getDraftMagazines() {
        List<MagazineDraftListResponseDto> draftList = magazineService.getDraftMagazines();

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, draftList));
    }
}
