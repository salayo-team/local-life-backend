package com.salayo.locallifebackend.domain.program.dto;

import com.salayo.locallifebackend.domain.program.enums.DayName;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProgramDayResponseDto {

	private Long id; //체험 프로그램 요일 고유 식별자

	private DayName dayName; //요일 이름
}
