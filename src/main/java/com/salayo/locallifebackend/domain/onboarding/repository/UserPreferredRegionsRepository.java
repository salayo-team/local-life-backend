package com.salayo.locallifebackend.domain.onboarding.repository;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.onboarding.entity.UserPreferredRegions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferredRegionsRepository extends JpaRepository<UserPreferredRegions, Long> {

	List<UserPreferredRegions> findByMember(Member member);

	List<UserPreferredRegions> findByMemberAndIsActiveTrue(Member member);

	Optional<UserPreferredRegions> findByMemberAndRegionName(Member member, String regionName);

	void deleteAllByMember(Member member);

	/**
	 * 특정 회원의 특정 지역 선호 여부 확인
	 */
	boolean existsByMemberAndRegionNameAndIsActiveTrue(Member member, String regionName);

	/**
	 * 특정 회원의 선호 지역 개수 조회
	 */
	long countByMemberAndIsActiveTrue(Member member);
}
