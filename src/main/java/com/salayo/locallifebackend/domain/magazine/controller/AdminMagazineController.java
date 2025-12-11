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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @Operation(
        summary = "매거진 임시 저장 (1차 등록)",
        description = """
            관리자가 로컬 크리에이터 인터뷰 내용을 기반으로 매거진을 작성 후 임시 저장(DRAFT)합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "임시 저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "로컬 크리에이터 또는 카테고리 조회 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping
    public ResponseEntity<CommonResponseDto<Void>> createMagazine(
        @RequestBody @Valid MagazineCreateRequestDto createRequestDto,
        @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        magazineService.createMagazine(createRequestDto, memberDetails.getMember().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, null));
    }

    @Operation(
        summary = "매거진 파일 업로드",
        description = """
            매거진 내에서 사용되는 이미지 파일을 업로드합니다.

            filePurpose 종류
            - THUMBNAIL: 썸네일(1장 제한)  
            - DETAIL_IMAGE: 본문 상세 이미지(여러 장 가능)

            제약  
            - THUMBNAIL은 1장만 허용합니다.  
            - detail 이미지 업로드 후 magazine 생성 시 updateReferenceId로 자동 연결됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업로드 성공"),
        @ApiResponse(responseCode = "400", description = "썸네일 개수 초과 또는 잘못된 파일"),
    })
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponseDto<List<MagazineFileUploadResponseDto>>> uploadFiles(
        @RequestPart("files") @NotNull List<MultipartFile> files,
        @RequestParam("purpose") FilePurpose filePurpose
    ) {
        List<MagazineFileUploadResponseDto> uploadResponseDto = magazineFileService.uploadFiles(files, filePurpose);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FILE_UPLOAD_SUCCESS, uploadResponseDto));
    }

    @Operation(
        summary = "임시 저장 매거진 목록 조회",
        description = """
            DRAFT / REQUEST_REVISION / PENDING_CONFIRMATION 상태의 매거진 목록을 조회합니다.

            기본 정렬: 생성일 최신순  
            Pagination 응답 형식: PaginationResponseDto
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "페이지/사이즈 값 오류")
    })
    @GetMapping
    public ResponseEntity<CommonResponseDto<PaginationResponseDto<MagazineDraftListResponseDto>>> getDraftMagazines(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PaginationResponseDto<MagazineDraftListResponseDto> draftList = magazineService.getDraftMagazines(page, size);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, draftList));
    }

    @Operation(
        summary = "임시 저장 매거진 상세 조회",
        description = """
            DRAFT 상태의 매거진 상세 내용을 조회합니다.

            포함 정보:
            - 제목, 내용  
            - 썸네일  
            - 상세 이미지 목록(detailImageUrls)  
            - 카테고리 정보  
            - 관리자 정보  
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "매거진을 찾을 수 없음")
    })
    @GetMapping("/{magazineId}")
    public ResponseEntity<CommonResponseDto<MagazineDraftDetailResponseDto>> getDraftMagazineDetail(@PathVariable Long magazineId) {
        MagazineDraftDetailResponseDto draftDetail = magazineService.getDraftMagazineDetail(magazineId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, draftDetail));
    }

    @Operation(
        summary = "매거진 초안 확인 이메일 발송",
        description = """
            로컬 크리에이터에게 초안 확인 링크를 이메일로 전송합니다.

            보안 방식  
            - PreviewToken 기반  
            - 유효기간: 14일  
            - 로컬 크리에이터 본인만 접근 가능

            매거진 상태는 PENDING_CONFIRMATION으로 자동 변경됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "메일 발송 성공"),
        @ApiResponse(responseCode = "404", description = "매거진을 찾을 수 없음")
    })
    @PostMapping("/{magazineId}/preview/send")
    public ResponseEntity<CommonResponseDto<Void>> sendMagazinePreviewLink(@PathVariable Long magazineId) {
        magazineService.sendPreviewLink(magazineId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.EMAIL_SEND_SUCCESS, null));
    }

    @Operation(
        summary = "매거진 피드백 반영 수정",
        description = """
            로컬 크리에이터가 남긴 피드백을 바탕으로 매거진을 수정합니다.

            제약 조건  
            - 피드백 미반영 상태가 1개 이상이면 수정 불가  
            - 수정 이력은 최대 3회까지 저장  
            - 수정 시 MagazineRevision 엔티티 생성
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "수정 제한 초과 또는 피드백 미반영 상태 존재"),
        @ApiResponse(responseCode = "404", description = "매거진 또는 피드백 없음"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PutMapping("/{magazineId}/feedback")
    public ResponseEntity<CommonResponseDto<Void>> updateMagazineFromFeedback(
        @PathVariable Long magazineId,
        @RequestBody @Valid MagazineUpdateRequestDto magazineUpdateRequestDto,
        @AuthenticationPrincipal MemberDetails memberDetails) {
        magazineService.updateMagazineFromFeedback(magazineId, magazineUpdateRequestDto, memberDetails.getMember());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    @Operation(
        summary = "매거진 수정 이력 조회",
        description = """
            해당 매거진의 전체 수정본(revision) 목록을 조회합니다.

            반환 정보  
            - revisionNumber  
            - 수정된 content  
            - timestamp  
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 이력 조회 성공",
            content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "매거진을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @GetMapping("/{magazineId}/revisions")
    public ResponseEntity<CommonResponseDto<List<MagazineRevisionResponseDto>>> getMagazineRevisions(
        @PathVariable Long magazineId
    ) {
        List<MagazineRevisionResponseDto> revisions = magazineRevisionService.getRevisions(magazineId);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, revisions));
    }

    @Operation(
        summary = "매거진 수정본 확인 링크 발송",
        description = """
            매거진 수정 이력이 존재할 경우 로컬 크리에이터에게 확인 링크를 전송합니다.

            조건  
            - 수정 이력(revision)이 1개 이상 있어야 함  
            - 기존 previewToken이 유효해야 함  
            - 재발급 시 유효기간은 3일  
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정본 확인 링크 발송 성공",
            content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "수정 이력이 없거나 previewToken이 만료됨"),
        @ApiResponse(responseCode = "404", description = "매거진을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @PostMapping("/{magazineId}/revision/send")
    public ResponseEntity<CommonResponseDto<Void>> sendMagazineRevisionLink(@PathVariable Long magazineId) {
        magazineService.sendRevisionLink(magazineId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.EMAIL_SEND_SUCCESS, null));
    }

    @Operation(
        summary = "관리자 직접 수정",
        description = """
            피드백과 관계없이, 관리자가 매거진 본문을 직접 수정합니다.

            사용 예  
            - 오타 정정  
            - 문장 자연스러움 보완  
            - 기타 편집 목적 수정

            revisionNumber=0으로 기록됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매거진 수정 성공",
            content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "매거진을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @PutMapping("/{magazineId}")
    public ResponseEntity<CommonResponseDto<Void>> updateMagazine(@PathVariable Long magazineId,
        @RequestBody @Valid MagazineUpdateRequestDto magazineUpdateRequestDto,
        @AuthenticationPrincipal MemberDetails memberDetails) {
        magazineService.updateMagazine(magazineId, magazineUpdateRequestDto, memberDetails.getMember());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    @Operation(
        summary = "임시 저장 매거진 삭제",
        description = """
            DRAFT 상태의 매거진을 Soft Delete 처리합니다.

            삭제 불가능한 경우  
            - 이미 Soft Delete  
            - REGISTERED 상태  
            - REQUEST_REVISION / PENDING_CONFIRMATION 상태도 삭제 불가
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매거진 삭제 성공",
            content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "삭제할 수 없는 상태"),
        @ApiResponse(responseCode = "404", description = "매거진을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @DeleteMapping("/{magazineId}")
    public ResponseEntity<CommonResponseDto<Void>> deleteDraftMagazine(
        @PathVariable Long magazineId,
        @AuthenticationPrincipal MemberDetails memberDetails) {
        magazineService.deleteDraftMagazine(magazineId, memberDetails.getMember().getId());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.DELETE_SUCCESS, null));
    }

    @Operation(
        summary = "매거진 협업 종료",
        description = """
            로컬 크리에이터의 최종 승인 또는 협업 중단 시 매거진을 DRAFT로 되돌립니다.

            조건  
            - 상태가 REQUEST_REVISION 또는 PENDING_CONFIRMATION이어야 함  
            - 수정 이력이 3회를 초과할 경우 협업 종료 불가
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "협업 종료 처리 성공",
            content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "협업 종료가 불가능한 상태"),
        @ApiResponse(responseCode = "404", description = "매거진을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @PostMapping("/{magazineId}/finalize")
    public ResponseEntity<CommonResponseDto<Void>> finalizeCollaboration(@PathVariable Long magazineId) {
        magazineService.finalizeCollaboration(magazineId);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    @Operation(
        summary = "매거진 최종 등록",
        description = "관리자가 매거진을 최종 등록(REGISTERED) 상태로 변경합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "등록 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "매거진 찾을 수 없음")
    })
    @PostMapping("/{magazineId}/register")
    public ResponseEntity<CommonResponseDto<Void>> registerMagazine(
        @PathVariable Long magazineId,
        @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        magazineService.registerMagazine(magazineId, memberDetails.getMember().getId());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, null));
    }
}
