package com.salayo.locallifebackend.domain.program.repository;

import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long>, ProgramRepositoryCustom{

	default Program findByIdOrElseThrow(long programId){
		return findById(programId).orElseThrow(() -> new CustomException(ErrorCode.PROGRAM_NOT_FOUND));
	}

}
