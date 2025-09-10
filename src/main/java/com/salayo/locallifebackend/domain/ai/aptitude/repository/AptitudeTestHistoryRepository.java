package com.salayo.locallifebackend.domain.ai.aptitude.repository;

import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestHistory;
import com.salayo.locallifebackend.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AptitudeTestHistoryRepository extends JpaRepository<AptitudeTestHistory, Long> {

	// 멤버의 모든 이력 삭제 (테스트 시작 시 초기화용)
	void deleteAllByMember(Member member);
	
	// 멤버의 미완료 이력만 삭제
	void deleteByMemberAndIsCompletedFalse(Member member);
	
	// 세션별 이력 조회
	List<AptitudeTestHistory> findBySessionIdOrderByStepAsc(String sessionId);
	
	// 완료된 테스트 이력 조회
	List<AptitudeTestHistory> findByMemberAndIsCompletedTrueOrderByCreatedAtDesc(Member member);
	
	// 특정 멤버의 마지막 세션 조회 (추후 "이전 테스트 이어하기" 기능용)
	Optional<AptitudeTestHistory> findTopByMemberOrderByCreatedAtDesc(Member member);
	
	// 세션별 테스트 완료 처리
	@Modifying
	@Query("UPDATE AptitudeTestHistory h SET h.isCompleted = true WHERE h.sessionId = :sessionId")
	void markSessionAsCompleted(@Param("sessionId") String sessionId);
	
	// 미완료 세션 조회
	Optional<AptitudeTestHistory> findTopByMemberAndIsCompletedFalseOrderByCreatedAtDesc(Member member);
	
	// 세션의 최대 step 조회
	@Query("SELECT MAX(h.step) FROM AptitudeTestHistory h WHERE h.sessionId = :sessionId")
	Optional<Integer> findMaxStepBySessionId(@Param("sessionId") String sessionId);
}
