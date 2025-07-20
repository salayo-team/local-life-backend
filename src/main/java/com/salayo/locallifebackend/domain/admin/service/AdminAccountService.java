package com.salayo.locallifebackend.domain.admin.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.enums.Gender;
import com.salayo.locallifebackend.domain.member.enums.MemberRole;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAccountService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createAdmin(String email, String rawPassword, String nickname) {
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member admin = Member.builder()
            .email(email)
            .encodedPassword(passwordEncoder.encode(rawPassword))
            .nickname(nickname)
            .birth("1990")
            .gender(Gender.UNKNOWN)
            .memberRole(MemberRole.ADMIN)
            .build();

        memberRepository.save(admin);
    }
}
