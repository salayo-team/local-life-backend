package com.salayo.locallifebackend.domain.localcreator.repository;

import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalCreatorRepository extends JpaRepository<LocalCreator, Long> {

    Optional<LocalCreator> findByMemberId(Long memberId);

}
