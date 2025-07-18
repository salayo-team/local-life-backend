package com.salayo.locallifebackend.domain.program.repository;

import com.salayo.locallifebackend.domain.program.entity.ProgramScheduleTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramScheduleTimeRepository extends JpaRepository<ProgramScheduleTime, Long> {

}
