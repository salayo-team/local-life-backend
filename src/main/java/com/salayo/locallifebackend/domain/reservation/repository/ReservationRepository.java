package com.salayo.locallifebackend.domain.reservation.repository;


import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	default Reservation findByIdOrElseThrow(long reservationId){
		return findById(reservationId).orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
	}

	List<Reservation> findAllByProgramSchedule_IdIn(List<Long> scheduleIds);
}
