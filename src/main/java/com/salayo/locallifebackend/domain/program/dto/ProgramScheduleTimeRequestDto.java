package com.salayo.locallifebackend.domain.program.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ProgramScheduleTimeRequestDto {

	@NotNull(message = "스케줄 회차는 필수값입니다.")
	@Min(value = 1, message = "스케줄 회차는 최소 {value}회차 이상이어야 합니다.")
	@Max(value = 20, message = "스케줄 회차는 최대 {value}회차 이하여야 합니다.")
	private Integer scheduleCount; //스케줄 회차

	@NotNull(message = "스케줄 소요 시간은 필수값입니다.")
	@Min(value = 1, message = "스케줄 소요 시간은 최소 {value}시간 이상이어야 합니다.")
	@Max(value = 20, message = "스케줄 소요 시간은 최대 {value}시간 이하여야 합니다.")
	private Integer scheduleDuration; //스케줄 소요 시간

	@NotNull(message = "스케줄 시작 시간은 필수값입니다.")
	private LocalTime startTime; //스케줄 시작시간

}
