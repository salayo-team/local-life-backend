package com.salayo.locallifebackend.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "리뷰 작성/수정 요청 DTO")
public class ReviewRequestDto {

	@Schema(
		description = "리뷰 내용",
		example = "공간도 예쁘고 로컬 크리에이터님도 너무 친절했어요!",
		required = true,
		maxLength = 500
	)
	@NotBlank(message = "리뷰 내용은 필수입니다.")
	@Size(max = 500, message = "리뷰는 최대 500자까지 작성 가능합니다.")
	private String content;

	@Schema(
		description = "리뷰 별점 (0.0 ~ 5.0, 0.5 단위)",
		example = "4.5",
		required = true,
		minimum = "0.0",
		maximum = "5.0"
	)
	@NotNull(message = "별점은 필수입니다.")
	private BigDecimal reviewRating;

	@Schema(
		description = "선택한 리뷰 태그 ID 목록",
		example = "[1, 3, 5]"
	)
	private List<Long> tagIds;
}
