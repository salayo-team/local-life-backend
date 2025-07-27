package com.salayo.locallifebackend.domain.magazine.repository;

import com.salayo.locallifebackend.domain.magazine.entity.Magazine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MagazineRepository extends JpaRepository<Magazine, Long> {

}
