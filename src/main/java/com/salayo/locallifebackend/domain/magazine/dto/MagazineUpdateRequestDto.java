package com.salayo.locallifebackend.domain.magazine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MagazineUpdateRequestDto {

    @NotBlank(message = "매거진 수정 내용을 입력해주세요.")
    String content;

}
