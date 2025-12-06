package com.salayo.locallifebackend.domain.localcreator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LocalCreatorUpdateRequestDto {

    @NotBlank(message = "상호명은 필수입니다.")
    private String businessName;

    @NotBlank(message = "사업장주소는 필수입니다.")
    private String businessAddress;
}
