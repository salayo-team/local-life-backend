package com.salayo.locallifebackend.domain.magazine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MagazineCreateRequestDto {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "본문 내용을 입력해주세요.")
    private String content;

    @NotNull(message = "지역 카테고리를 선택해주세요.")
    private Long regionCategoryId;

    @NotNull(message = "적성 카테고리를 선택해주세요.")
    private Long aptitudeCategoryId;

    @NotBlank(message = "대포 이미지를 등록해주세요.")
    private String thumbnailUrl;

}
