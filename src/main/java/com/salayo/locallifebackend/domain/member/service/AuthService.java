package com.salayo.locallifebackend.domain.member.service;

import com.salayo.locallifebackend.domain.member.dto.UserSignupRequestDto;
import com.salayo.locallifebackend.domain.member.dto.UserSignupResponseDto;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.enums.Gender;
import com.salayo.locallifebackend.domain.member.enums.MemberRole;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.member.util.NicknameGenerator;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserSignupResponseDto signupUser(UserSignupRequestDto requestDto) {

        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        String nickname = requestDto.getNickname();
        if (nickname == null || nickname.isBlank()) {

            do {
                nickname = NicknameGenerator.generate();
            } while (memberRepository.findByNickname(nickname).isPresent());
        } else if (memberRepository.findByNickname(nickname).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        Member member = Member.builder()
            .email(requestDto.getEmail())
            .encodedPassword(passwordEncoder.encode(requestDto.getPassword()))
            .birth(requestDto.getBirth())
            .nickname(nickname)
            .gender(Gender.FEMALE)
            .memberRole(MemberRole.USER)
            .build();

        memberRepository.save(member);

        return new UserSignupResponseDto(member.getNickname(), "일반회원");
    }
}
