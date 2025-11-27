package com.salayo.locallifebackend.domain.member.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.util.RedisUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, RedisUtil redisUtil) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisUtil = redisUtil;
    }

    @Transactional
    public void updatePassword(String email, String currentPassword, String newPassword) {
        Member member = memberRepository.findByEmailOrThrow(email);

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new CustomException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        member.updatePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void withdraw(Long memberId, String currentPassword ,String reason) {
        Member member = memberRepository.findActiveByIdOrThrow(memberId);

        if (member.isDeleted()) {
            throw new CustomException(ErrorCode.ALREADY_DELETED_MEMBER);
        }

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        member.withdraw(reason);

        redisUtil.deleteRefreshToken(member.getId());
        redisUtil.deleteAccessToken(member.getId());
    }

}
