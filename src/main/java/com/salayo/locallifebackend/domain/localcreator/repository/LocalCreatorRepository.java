package com.salayo.locallifebackend.domain.localcreator.repository;

import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalCreatorRepository extends JpaRepository<LocalCreator, Long> {

	LocalCreator findByUserIdAndCreatorStatus(Long id, String approved);
}
