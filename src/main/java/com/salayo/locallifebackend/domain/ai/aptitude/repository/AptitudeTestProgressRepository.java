package com.salayo.locallifebackend.domain.ai.aptitude.repository;

import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestProgress;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.UserAptitude;
import com.salayo.locallifebackend.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AptitudeTestProgressRepository extends JpaRepository<AptitudeTestProgress, Long> {

	Optional<AptitudeTestProgress> findByUserAptitude(UserAptitude userAptitude);

	@Query("SELECT p FROM AptitudeTestProgress p WHERE p.userAptitude.member = :member")
	Optional<AptitudeTestProgress> findByMember(@Param("member") Member member);
}
