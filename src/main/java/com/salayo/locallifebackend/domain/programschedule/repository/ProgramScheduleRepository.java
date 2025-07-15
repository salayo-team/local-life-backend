package com.salayo.locallifebackend.domain.programschedule.repository;

import com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramScheduleRepository extends JpaRepository<ProgramSchedule, Long> {

}
