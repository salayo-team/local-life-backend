package com.salayo.locallifebackend.domain.magazine.controller;

import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineCreateRequestDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineDraftDetailResponseDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineDraftListResponseDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineFileUploadResponseDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineRevisionResponseDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineUpdateRequestDto;
import com.salayo.locallifebackend.domain.magazine.service.MagazineFileService;
import com.salayo.locallifebackend.domain.magazine.service.MagazineRevisionService;
import com.salayo.locallifebackend.domain.magazine.service.MagazineService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.dto.PaginationResponseDto;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/magazines")
@Tag(name = "Magazine | Admin", description = "관리자 매거진 관리 API")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMagazineController {

    private final MagazineService magazineService;
    private final MagazineFileService magazineFileService;
    private final MagazineRevisionService magazineRevisionService;

    public AdminMagazineController(MagazineService magazineService, MagazineFileService magazineFileService,
        MagazineRevisionService magazineRevisionService) {
        this.magazineService = magazineService;
        this.magazineFileService = magazineFileService;
        this.magazineRevisionService = magazineRevisionService;
    }

    @Operation(summary = "매거진 임시 저장 - 로컬 크리에이터 확인용", description = "관리자가 로컬 크리에이터를 인터뷰한 매거진을 임시 상태로 저장")
    @PostMapping
    public ResponseEntity<CommonResponseDto<Void>> createMagazine(
        @RequestBody @Valid MagazineCreateRequestDto createRequestDto,
        @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        magazineService.createMagazine(createRequestDto, memberDetails.getMember().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, null));
    }

    @Operation(summary = "매거진 파일 업로드", description = "매거진 썸네일/상세이미지 업로드(purpose=THUMBNAIL/DETAIL_IMAGE)")
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponseDto<List<MagazineFileUploadResponseDto>>> uploadFiles(
        @RequestPart("files") @NotNull List<MultipartFile> files,
        @RequestParam("purpose") FilePurpose filePurpose
    ) {
        List<MagazineFileUploadResponseDto> uploadResponseDto = magazineFileService.uploadFiles(files, filePurpose);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FILE_UPLOAD_SUCCESS, uploadResponseDto));
    }

    @Operation(summary = "1차등록(임시등록) 매거진 목록 조회", description = "관리자가 1차등록 매거진 목록을 조회")
    @GetMapping
    public ResponseEntity<CommonResponseDto<PaginationResponseDto<MagazineDraftListResponseDto>>> getDraftMagazines(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PaginationResponseDto<MagazineDraftListResponseDto> draftList = magazineService.getDraftMagazines(page, size);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, draftList));
    }

    @Operation(summary = "1차등록(임시등록) 매거진 상세 조회", description = "관리자가 1차등록 매거진의 상세 글을 조회")
    @GetMapping("/{magazineId}")
    public ResponseEntity<CommonResponseDto<MagazineDraftDetailResponseDto>> getDraftMagazineDetail(@PathVariable Long magazineId) {
        MagazineDraftDetailResponseDto draftDetail = magazineService.getDraftMagazineDetail(magazineId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, draftDetail));
    }

    @Operation(summary = "매거진 초안 확인 링크 전송", description = "로컬크리에이터에게 매거진 초안 확인용 링크를 이메일로 전송합니다.")
    @PostMapping("/{magazineId}/preview/send")
    public ResponseEntity<CommonResponseDto<Void>> sendMagazinePreviewLink(@PathVariable Long magazineId) {
        magazineService.sendPreviewLink(magazineId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.EMAIL_SEND_SUCCESS, null));
    }

    @Operation(summary = "피드백 기반 매거진 수정(최대 3회)", description = "관리자가 로컬크리에이터의 피드백에 따라 매거진 본문을 수정하며, 수정은 최대 3회, 수정 기록도 최대 3회 저장됩니다.")
    @PutMapping("/{magazineId}/feedback")
    public ResponseEntity<CommonResponseDto<Void>> updateMagazineFromFeedback(
        @PathVariable Long magazineId,
        @RequestBody @Valid MagazineUpdateRequestDto magazineUpdateRequestDto,
        @AuthenticationPrincipal MemberDetails memberDetails) {
        magazineService.updateMagazineFromFeedback(magazineId, magazineUpdateRequestDto, memberDetails.getMember());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    @Operation(summary = "매거진 수정 이력 조회", description = "관리자가 특정 매거진의 수정 이력 전체를 조회합니다.")
    @GetMapping("/{magazineId}/revisions")
    public ResponseEntity<CommonResponseDto<List<MagazineRevisionResponseDto>>> getMagazineRevisions(
        @PathVariable Long magazineId
    ) {
        List<MagazineRevisionResponseDto> revisions = magazineRevisionService.getRevisions(magazineId);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, revisions));
    }

    @Operation(summary = "매거진 수정본 확인 링크 전송", description = "로컬크리에이터에게 매거진 수정본 확인용 링크를 이메일로 전송합니다.")
    @PostMapping("/{magazineId}/revision/send")
    public ResponseEntity<CommonResponseDto<Void>> sendMagazineRevisionLink(@PathVariable Long magazineId) {
        magazineService.sendRevisionLink(magazineId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.EMAIL_SEND_SUCCESS, null));
    }

    @Operation(summary = "매거진 관리자 전용 수정", description = "관리자가 매거진 본문을 수정합니다. (피드백과 무관 -> 오타나 실수가 있었을 경우)")
    @PutMapping("/{magazineId}")
    public ResponseEntity<CommonResponseDto<Void>> updateMagazine(@PathVariable Long magazineId,
        @RequestBody @Valid MagazineUpdateRequestDto magazineUpdateRequestDto,
        @AuthenticationPrincipal MemberDetails memberDetails) {
        magazineService.updateMagazine(magazineId, magazineUpdateRequestDto, memberDetails.getMember());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }
}
