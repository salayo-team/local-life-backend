package com.salayo.locallifebackend.domain.member.dto;

import lombok.Getter;

@Getter
public class UserSignupResponseDto {

    private final String nickname;
    private final String role;

    public UserSignupResponseDto(String nickname, String role) {
        this.nickname = nickname;
        this.role = role;
    }
}
