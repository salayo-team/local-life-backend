package com.salayo.locallifebackend.domain.file.repository;

import com.salayo.locallifebackend.domain.file.entity.FileMapping;
import com.salayo.locallifebackend.domain.file.enums.FileCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMappingRepository extends JpaRepository<FileMapping, Long> {

    List<FileMapping> findAllByReferenceIdAndFileCategory(Long referenceId, FileCategory fileCategory);

}
