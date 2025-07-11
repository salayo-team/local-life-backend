package com.salayo.locallifebackend.domain.program.service;

import com.salayo.locallifebackend.domain.program.dto.ProgramCreateRequestDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateResponseDto;
import com.salayo.locallifebackend.domain.program.entity.Program;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgramService {

	/**
	 * 체험 프로그램 생성
	 */
	@Transactional
	public ProgramCreateResponseDto createProgram(long userId, ProgramCreateRequestDto requestDto) {

		//유저 롤 검증

		//체험 프로그램 생성
		Program program = Program.builder()
			.build();

		return ProgramCreateResponseDto.builder().message("체험 프로그램 생성이 완료되었습니다.").build();
	}

}
