package com.salayo.locallifebackend.domain.ai.aptitude.controller;

import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeSelectRequestDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.UserAptitudeResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.service.UserAptitudeService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/my-aptitude")
@PreAuthorize("hasRole('USER')")
@Tag(name = "MyAptitude", description = "사용자 적성 관리 API")
public class UserAptitudeController {

	private final UserAptitudeService userAptitudeService;

	public UserAptitudeController(UserAptitudeService userAptitudeService) {
		this.userAptitudeService = userAptitudeService;
	}

	@GetMapping("/info")
	@Operation(
		summary = "나의 적성 조회",
		description = "마이페이지에서 현재 설정된 나의 적성을 조회합니다. 온보딩이 완료된 사용자만 조회할 수 있습니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "나의 적성 정보를 성공적으로 조회",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "온보딩이 완료되지 않았거나 요청 값이 올바르지 않은 경우"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증이 필요하거나 토큰이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "403",
			description = "USER 권한이 아니거나 해당 자원에 대한 접근 권한이 없는 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "회원 또는 온보딩 진행 상태를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류가 발생한 경우"
		)
	})
	public ResponseEntity<CommonResponseDto<UserAptitudeResponseDto>> getMyAptitude(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

		UserAptitudeResponseDto responseDto = userAptitudeService.getMyAptitude(memberDetails.getMember().getId());

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responseDto));
	}

	@PutMapping("/settings")
	@Operation(
		summary = "나의 적성 수정",
		description = "마이페이지에서 나의 적성을 수동으로 변경합니다. 온보딩이 완료된 사용자만 수정할 수 있습니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "나의 적성 정보를 성공적으로 수정",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "온보딩이 완료되지 않았거나 잘못된 적성 타입 등 요청 값이 올바르지 않은 경우"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증이 필요하거나 토큰이 유효하지 않은 경우"
		),
		@ApiResponse(
			responseCode = "403",
			description = "USER 권한이 아니거나 해당 자원에 대한 접근 권한이 없는 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "회원 또는 온보딩 진행 상태를 찾을 수 없는 경우"
		),
		@ApiResponse(
			responseCode = "500",
			description = "서버 내부 오류가 발생한 경우"
		)
	})
	public ResponseEntity<CommonResponseDto<UserAptitudeResponseDto>> updateMyAptitude(
		@Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails,
		@RequestBody AptitudeSelectRequestDto requestDto) {

		UserAptitudeResponseDto responseDto = userAptitudeService.updateMyAptitude(memberDetails.getMember().getId(),
			requestDto.aptitudeType());

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, responseDto));
	}
}
