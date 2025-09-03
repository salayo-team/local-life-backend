package com.salayo.locallifebackend.domain.magazine.feedback.controller;

import com.salayo.locallifebackend.domain.magazine.feedback.dto.MagazineFeedbackCreateRequestDto;
import com.salayo.locallifebackend.domain.magazine.feedback.dto.MagazineFeedbackResponseDto;
import com.salayo.locallifebackend.domain.magazine.feedback.service.MagazineFeedbackService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/localcreator/magazines/{magazineId}/feedbacks")
@Tag(name = "Magazine Feedback | LocalCreator", description = "로컬 크리에이터 매거진 피드백 API")
public class MagazineFeedbackController {

    private final MagazineFeedbackService magazineFeedbackService;

    public MagazineFeedbackController(MagazineFeedbackService magazineFeedbackService) {
        this.magazineFeedbackService = magazineFeedbackService;
    }

    @Operation(summary = "매거진 피드백(수정 요청) 등록", description = "로컬 크리에이터가 본인의 인터뷰 매거진을 보고 피드백을 남깁니다. (최대 3회)")
    @PreAuthorize("hasRole('LOCAL_CREATOR')")
    @PostMapping
    public ResponseEntity<CommonResponseDto<Void>> createFeedback(
        @PathVariable Long magazineId,
        @RequestBody @Valid MagazineFeedbackCreateRequestDto createRequestDto,
        @AuthenticationPrincipal MemberDetails memberDetails) {

        magazineFeedbackService.createFeedback(magazineId, memberDetails.getMember().getId(), createRequestDto);

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, null));
    }

    @Operation(summary = "내가 작성한 피드백 조회", description = "로컬 크리에이터가 본인의 인터뷰 매거진에 남긴 피드백 목록을 조회합니다.")
    @PreAuthorize("hasRole('LOCAL_CREATOR')")
    @GetMapping
    public ResponseEntity<CommonResponseDto<List<MagazineFeedbackResponseDto>>> getMyFeedbacks(
        @PathVariable Long magazineId,
        @AuthenticationPrincipal MemberDetails memberDetails) {

        List<MagazineFeedbackResponseDto> feedbacks = magazineFeedbackService
            .getMyFeedbacks(magazineId, memberDetails.getMember().getId());

        return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, feedbacks));
    }
}
