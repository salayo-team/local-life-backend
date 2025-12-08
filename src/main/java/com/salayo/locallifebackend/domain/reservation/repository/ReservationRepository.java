package com.salayo.locallifebackend.domain.reservation.repository;


import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	default Reservation findByIdOrElseThrow(long reservationId) {
		return findById(reservationId).orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
	}

	boolean existsByProgramSchedule_IdInAndReservationStatusNotIn(
		List<Long> scheduleIds,
		Set<ReservationStatus> inactiveReservationStatuses
	);

	boolean existsByProgramScheduleIdAndReservationStatusNotIn(
		Long scheduleId,
		Set<ReservationStatus> inactiveReservationStatuses
	);

	boolean existsByIdAndReservationStatusIn(
		Long reservationId,
		Set<ReservationStatus> refundableReservationStatuses
	);

	Optional<Reservation> findByMemberId(Long memberId);

}
