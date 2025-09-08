package com.salayo.locallifebackend.domain.localcreator.dto;

import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import lombok.Getter;

@Getter
public class LocalCreatorEmailSearchResponseDto {

    private final Long id;
    private final String email;
    private final String businessName;

    public LocalCreatorEmailSearchResponseDto(LocalCreator localCreator) {
        this.id = localCreator.getId();
        this.businessName = localCreator.getBusinessName();
        this.email = localCreator.getMember().getEmail();
    }
}
