package com.salayo.locallifebackend.domain.admin.dto;

import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreatorPendingResponseDto {

    private Long id;
    private String email;
    private String businessName;
    private String businessAddress;
    private String createdAt;

    public static CreatorPendingResponseDto from(LocalCreator creator) {
        return CreatorPendingResponseDto.builder()
            .id(creator.getId())
            .email(creator.getMember().getEmail())
            .businessName(creator.getBusinessName())
            .businessAddress(creator.getBusinessAddress())
            .createdAt(creator.getCreatedAt().toString())
            .build();
    }

}
