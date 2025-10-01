package com.salayo.locallifebackend.domain.onboarding.repository;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.onboarding.entity.OnboardingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnboardingProgressRepository extends JpaRepository<OnboardingProgress, Long> {
    
    /**
     * 회원별 온보딩 진행 상태 조회
     */
    Optional<OnboardingProgress> findByMember(Member member);
}
