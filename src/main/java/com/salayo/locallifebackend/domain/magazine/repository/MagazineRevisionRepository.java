package com.salayo.locallifebackend.domain.magazine.repository;

import com.salayo.locallifebackend.domain.magazine.entity.MagazineRevision;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MagazineRevisionRepository extends JpaRepository<MagazineRevision, Long> {

    int countByMagazineId(Long magazineId);

    List<MagazineRevision> findAllByMagazineIdOrderByRevisionNumberAsc(Long magazineId);

}
