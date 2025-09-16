package com.salayo.locallifebackend.domain.onboarding.repository;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.onboarding.entity.OnboardingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 온보딩 진행 상태 Repository
 */
@Repository
public interface OnboardingProgressRepository extends JpaRepository<OnboardingProgress, Long> {
    
    /**
     * 회원별 온보딩 진행 상태 조회
     */
    Optional<OnboardingProgress> findByMember(Member member);
    
    /**
     * 회원 ID로 온보딩 진행 상태 조회
     */
    @Query("SELECT op FROM OnboardingProgress op WHERE op.member.id = :memberId")
    Optional<OnboardingProgress> findByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 세션 ID로 온보딩 진행 상태 조회
     */
    Optional<OnboardingProgress> findBySessionId(String sessionId);
    
    /**
     * 회원별 온보딩 완료 여부 확인
     */
    @Query("SELECT op.isCompleted FROM OnboardingProgress op WHERE op.member.id = :memberId")
    Optional<Boolean> isOnboardingCompletedByMemberId(@Param("memberId") Long memberId);
}
