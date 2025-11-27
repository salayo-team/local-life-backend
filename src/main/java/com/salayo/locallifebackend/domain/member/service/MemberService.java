package com.salayo.locallifebackend.domain.member.service;

import com.salayo.locallifebackend.domain.localcreator.repository.LocalCreatorRepository;
import com.salayo.locallifebackend.domain.member.dto.MemberInfoResponseDto;
import com.salayo.locallifebackend.domain.member.dto.MemberUpdateRequestDto;
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
    private final LocalCreatorRepository localCreatorRepository;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, RedisUtil redisUtil,
        LocalCreatorRepository localCreatorRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisUtil = redisUtil;
        this.localCreatorRepository = localCreatorRepository;
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
    public void withdraw(Long memberId, String currentPassword, String reason) {
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

    @Transactional
    public MemberInfoResponseDto updateMyInfo(Long memberId, MemberUpdateRequestDto memberUpdateRequestDto) {
        Member member = memberRepository.findActiveByIdOrThrow(memberId);

        if (memberUpdateRequestDto.getNickname() != null && !memberUpdateRequestDto.getNickname().equals(member.getNickname())) {

            if (memberRepository.existsByNickname(memberUpdateRequestDto.getNickname())) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }

            member.updateNickname(memberUpdateRequestDto.getNickname());
        }

        if (memberUpdateRequestDto.getPhoneNumber() != null && !memberUpdateRequestDto.getPhoneNumber().equals(member.getPhoneNumber())) {

            boolean existsInMember = memberRepository.existsByPhoneNumberAndIdNot(memberUpdateRequestDto.getPhoneNumber(), memberId);
            boolean existsInLocalcreator =
                localCreatorRepository.existsByMember_PhoneNumberAndMember_IdNot(memberUpdateRequestDto.getPhoneNumber(), memberId);

            if (existsInMember || existsInLocalcreator) {
                throw new CustomException(ErrorCode.DUPLICATE_PHONE_NUMBER);
            }

            member.updatePhoneNumber(memberUpdateRequestDto.getPhoneNumber());
        }

        return new MemberInfoResponseDto(member);
    }
}
