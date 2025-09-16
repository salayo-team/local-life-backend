package com.salayo.locallifebackend.domain.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 선호 지역 선택 요청 DTO
 */
@Getter
@NoArgsConstructor
public class OnboardingRegionRequestDto {
    
    @NotBlank(message = "지역을 선택해주세요")
    private String region;
    
//    private String subRegion;  // 세부 지역 (선택)
}
