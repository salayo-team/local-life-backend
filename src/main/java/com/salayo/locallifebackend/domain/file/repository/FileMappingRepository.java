package com.salayo.locallifebackend.domain.file.repository;

import com.salayo.locallifebackend.domain.file.entity.FileMapping;
import com.salayo.locallifebackend.domain.file.enums.FileCategory;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMappingRepository extends JpaRepository<FileMapping, Long> {

    List<FileMapping> findAllByReferenceIdAndFileCategory(Long referenceId, FileCategory fileCategory);

    List<FileMapping> findAllByFileCategoryAndReferenceIdAndFilePurpose(
        FileCategory fileCategory,
        Long referenceId,
        FilePurpose filePurpose
    );

    @Modifying
    @Query("UPDATE FileMapping f SET f.referenceId = :magazineId " +
            "WHERE f.file.storedFileName = :url AND f.referenceId = 0")
    int updateReferenceIdByStoredFileNameAndZeroReference(@Param("url") String url, @Param("magazineId") Long magazineId);

}
