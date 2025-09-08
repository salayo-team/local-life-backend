package com.salayo.locallifebackend.domain.magazine.repository;

import com.salayo.locallifebackend.domain.magazine.entity.Magazine;
import com.salayo.locallifebackend.domain.magazine.enums.MagazineStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MagazineRepository extends JpaRepository<Magazine, Long> {

    Page<Magazine> findByMagazineStatusAndDeletedStatus(
        MagazineStatus magazineStatus,
        DeletedStatus deletedStatus,
        Pageable pageable
    );
}
