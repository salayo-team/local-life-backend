package com.salayo.locallifebackend.domain.reservation.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReservationCreateRequestDto {

	@NotNull(message = "체험 프로그램 스케줄 ID는 필수값입니다.")
	private Long programScheduleId; //체험 프로그램 스케줄 고유 식별자

}
