package com.salayo.locallifebackend.domain.program.repository;

import com.salayo.locallifebackend.domain.program.entity.ProgramDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramDayRepository extends JpaRepository<ProgramDay, Long> {

}
