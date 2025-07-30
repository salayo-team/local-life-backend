package com.salayo.locallifebackend.domain.magazine.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MagazineDraftListResponseDto {

    private Long id;
    private String title;
    private String thumbnailUrl;
    private String regionName;
    private String aptitudeName;
    private LocalDateTime createdAt;

}
