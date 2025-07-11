package com.salayo.locallifebackend.domain.file.repository;

import com.salayo.locallifebackend.domain.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {

}
