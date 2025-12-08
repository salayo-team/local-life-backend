package com.salayo.locallifebackend.domain.programschedule.repository;

import com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramScheduleRepository extends JpaRepository<ProgramSchedule, Long> {

	default ProgramSchedule findByIdOrElseThrow(Long programScheduleId){
		return findById(programScheduleId).orElseThrow(() -> new CustomException(ErrorCode.PROGRAM_SCHEDULE_NOT_FOUND));
	};
}
