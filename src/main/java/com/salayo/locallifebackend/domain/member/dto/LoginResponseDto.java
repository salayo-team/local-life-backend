package com.salayo.locallifebackend.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {

    private final String accessToken;
    private final String refreshToken;

    public LoginResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
