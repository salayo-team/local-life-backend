package com.salayo.locallifebackend.domain.program.repository;

import com.salayo.locallifebackend.domain.program.entity.ProgramGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramGroupRepository extends JpaRepository<ProgramGroup, Long> {

}
