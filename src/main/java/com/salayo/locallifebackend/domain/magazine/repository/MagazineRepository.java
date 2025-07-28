package com.salayo.locallifebackend.domain.magazine.repository;

import com.salayo.locallifebackend.domain.magazine.entity.Magazine;
import com.salayo.locallifebackend.domain.magazine.enums.MagazineStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MagazineRepository extends JpaRepository<Magazine, Long> {

    List<Magazine> findByMagazineStatusAndDeletedStatus(MagazineStatus magazineStatus, DeletedStatus deletedStatus);
}
