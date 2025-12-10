package com.salayo.locallifebackend.domain.programschedule.repository;

import com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule;
import com.salayo.locallifebackend.domain.programschedule.enums.ProgramScheduleStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramScheduleRepository extends JpaRepository<ProgramSchedule, Long> {

	default ProgramSchedule findByIdOrElseThrow(Long programScheduleId) {
		return findById(programScheduleId).orElseThrow(() -> new CustomException(ErrorCode.PROGRAM_SCHEDULE_NOT_FOUND));
	}

	@Query("""
			select ps
			from ProgramSchedule ps
			where ps.program.id in(
				select p.id
				from Program p
				where p.member.id = :memberId
			)
			and ps.deletedStatus = :deletedStatus
			and ps.programScheduleStatus = :programScheduleStatus
			and ps.scheduleDate between :startDate and :endDate
		""")
	List<ProgramSchedule> findActiveSchedulesByMemberAndDateRange(
		@Param("memberId") Long memberId,
		@Param("deletedStatus") DeletedStatus deletedStatus,
		@Param("programScheduleStatus") ProgramScheduleStatus programScheduleStatus,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);
}
