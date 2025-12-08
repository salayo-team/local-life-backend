package com.salayo.locallifebackend.domain.magazine.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MagazineDetailResponseDto {

    private Long id;
    private String title;
    private String content;
    private String thumbnailUrl;
    private List<String> detailImageUrls;
    private String regionName;
    private String aptitudeName;
    private LocalDateTime registeredAt;
    private Long views;

}
