package com.salayo.locallifebackend.domain.localcreator.dto;

import lombok.Getter;

@Getter
public class LocalCreatorSignupResponseDto {

    private String businessName;
    private String role;

    public LocalCreatorSignupResponseDto(String businessName) {
        this.businessName = businessName;
        this.role = "로컬크리에이터";
    }

}
