package com.salayo.locallifebackend.domain.magazine.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MagazineDraftDetailResponseDto {

    private Long id;
    private String title;
    private String content;
    private String thumnailUrl;
    private List<String> detailImageUrls;
    private String regionName;
    private String aptitudeName;
    private String adminEmail;
    private LocalDateTime createdAt;

}
