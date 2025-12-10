package com.salayo.locallifebackend.domain.magazine.feedback.controller;

import com.salayo.locallifebackend.domain.magazine.feedback.dto.MagazineFeedbackCreateRequestDto;
import com.salayo.locallifebackend.domain.magazine.feedback.dto.MagazineFeedbackResponseDto;
import com.salayo.locallifebackend.domain.magazine.feedback.service.MagazineFeedbackService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/magazines/{magazineId}/feedbacks")
@Tag(name = "Magazine Feedback", description = "매거진 피드백 API")
public class MagazineFeedbackController {

    private final MagazineFeedbackService magazineFeedbackService;

    public MagazineFeedbackController(MagazineFeedbackService magazineFeedbackService) {
        this.magazineFeedbackService = magazineFeedbackService;
    }

    @Operation(
        summary = "매거진 피드백(수정 요청) 등록",
        description = """
            로컬 크리에이터가 본인의 매거진 초안/수정본을 보고 피드백(수정 요청)을 작성합니다.

            제약 조건  
            - 본인의 매거진이 아닐 경우 등록 불가  
            - 피드백은 **최대 3회까지 작성 가능**  
            - 이전 피드백이 반영(reflected=false) 상태라면 추가 작성 불가  
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "피드백 등록 성공"),
        @ApiResponse(responseCode = "400", description = "요청 데이터 오류 / 기존 피드백 미반영 상태"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)"),
        @ApiResponse(responseCode = "403", description = "본인의 매거진이 아님"),
        @ApiResponse(responseCode = "404", description = "매거진 또는 로컬 크리에이터 정보 없음"),
    })
    @PreAuthorize("hasRole('LOCAL_CREATOR')")
    @PostMapping
    public ResponseEntity<CommonResponseDto<Void>> createFeedback(
        @PathVariable Long magazineId,
        @RequestBody @Valid MagazineFeedbackCreateRequestDto createRequestDto,
        @AuthenticationPrincipal MemberDetails memberDetails) {

        magazineFeedbackService.createFeedback(magazineId, memberDetails.getMember().getId(), createRequestDto);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, null));
    }

    @Operation(
        summary = "매거진 피드백 조회",
        description = """
            매거진에 등록된 피드백을 조회합니다.

            조회 권한  
            - 로컬 크리에이터: 본인이 작성한 피드백만 조회  
            - 관리자: 해당 매거진의 피드백 조회 가능  
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "피드백 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "조회 권한 없음"),
        @ApiResponse(responseCode = "404", description = "매거진 또는 로컬 크리에이터 정보 없음")
    })
    @PreAuthorize("hasAnyRole('LOCAL_CREATOR','ADMIN')")
    @GetMapping
    public ResponseEntity<CommonResponseDto<List<MagazineFeedbackResponseDto>>> getFeedbacks(
        @PathVariable Long magazineId,
        @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        List<MagazineFeedbackResponseDto> feedbacks;
        if (memberDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            feedbacks = magazineFeedbackService.getFeedbacksForAdmin(magazineId);
        } else {
            feedbacks = magazineFeedbackService.getMyFeedbacks(magazineId, memberDetails.getMember().getId());
        }

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, feedbacks));
    }

    @Operation(
        summary = "매거진 피드백 삭제 (관리자 전용)",
        description = """
            관리자가 특정 매거진에 대한 피드백을 삭제합니다.

            유의사항  
            - 이미 반영 완료(reflected=true)된 피드백은 삭제할 수 없음  
            - 매거진에 속하지 않은 피드백 ID를 전달하면 오류 발생  
            - 로컬 크리에이터가 실수로 잘못 작성한 경우 삭제를 위한 기능  
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "피드백 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
        @ApiResponse(responseCode = "404", description = "피드백 또는 매거진 정보 없음"),
        @ApiResponse(responseCode = "409", description = "이미 반영된 피드백 삭제 불가")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<CommonResponseDto<Void>> deleteFeedback(
        @PathVariable Long magazineId,
        @PathVariable Long feedbackId
    ) {
        magazineFeedbackService.deleteFeedback(magazineId, feedbackId);
        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.DELETE_SUCCESS, null));
    }

}
