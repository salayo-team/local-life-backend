package com.salayo.locallifebackend.domain.user.repository;

import com.salayo.locallifebackend.domain.user.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
}
