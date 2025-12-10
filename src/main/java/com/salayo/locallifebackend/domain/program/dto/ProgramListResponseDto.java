package com.salayo.locallifebackend.domain.program.dto;

import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.enums.LocalSpecialized;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProgramListResponseDto {

	private final Long id;

	private final Long providerId;

	private final Long aptitudeCategoryId;

	private final Long regionCategoryId;

	private final String businessName;

	private final String title;

	private final String location;

	private final BigDecimal price;

	private final BigDecimal percent;

	private final BigDecimal finalPrice;

	private final LocalSpecialized isLocalSpecialized;

	private final ProgramStatus programStatus;

	private final String thumbnailUrl;

	private final Integer reviewCount;

	private final BigDecimal averageRating;

	public static ProgramListResponseDto from(Program program, String thumbnailUrl) {

		return ProgramListResponseDto.builder()
			.id(program.getId())
			.providerId(program.getMember().getId())
			.aptitudeCategoryId(program.getAptitudeCategory().getId())
			.regionCategoryId(program.getRegionCategory().getId())
			.businessName(program.getBusinessName())
			.title(program.getTitle())
			.location(program.getLocation())
			.price(program.getPrice())
			.percent(program.getPercent())
			.finalPrice(program.getFinalPrice())
			.isLocalSpecialized(program.getIsLocalSpecialized())
			.programStatus(program.getProgramStatus())
			.thumbnailUrl(thumbnailUrl)
			.reviewCount(program.getReviewCount())
			.averageRating(program.getAverageRating())
			.build();
	}

}
