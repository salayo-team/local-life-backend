package com.salayo.locallifebackend.domain.program.repository;

import com.salayo.locallifebackend.domain.program.dto.ProgramSearchRequestDto;
import com.salayo.locallifebackend.domain.program.entity.Program;
import org.springframework.data.domain.Page;

public interface ProgramRepositoryCustom {

	Page<Program> searchPrograms(ProgramSearchRequestDto requestDto);

}
