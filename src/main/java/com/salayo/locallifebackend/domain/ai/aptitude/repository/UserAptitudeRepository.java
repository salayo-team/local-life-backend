package com.salayo.locallifebackend.domain.ai.aptitude.repository;

import com.salayo.locallifebackend.domain.ai.aptitude.entity.UserAptitude;
import com.salayo.locallifebackend.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAptitudeRepository extends JpaRepository<UserAptitude, Long> {

	Optional<UserAptitude> findByMember(Member member);

	boolean existsByMember(Member member);

}
