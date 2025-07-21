package com.salayo.locallifebackend.domain.ai.aptitude.repository;

import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestHistory;
import com.salayo.locallifebackend.domain.member.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AptitudeTestHistoryRepository extends JpaRepository<AptitudeTestHistory, Long> {

	List<AptitudeTestHistory> findByMemberOrderByStepAsc(Member member);

	void deleteAllByMember(Member member);
}
