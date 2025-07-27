package com.salayo.locallifebackend.domain.program.repository;

import com.salayo.locallifebackend.domain.program.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long>, ProgramRepositoryCustom{

}
