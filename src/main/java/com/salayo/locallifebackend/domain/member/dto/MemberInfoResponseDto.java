package com.salayo.locallifebackend.domain.member.dto;

import com.salayo.locallifebackend.domain.member.entity.Member;
import lombok.Getter;

@Getter
public class MemberInfoResponseDto {

    private final String email;
    private final String nickname;
    private final String phoneNumber;
    private final String birth;
    private final String gender;
    private final String memberRole;

    public MemberInfoResponseDto(Member member) {
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.phoneNumber = member.getPhoneNumber();
        this.birth = member.getBirth();
        this.gender = member.getGender().name();
        this.memberRole = member.getMemberRole().name();
    }

}
