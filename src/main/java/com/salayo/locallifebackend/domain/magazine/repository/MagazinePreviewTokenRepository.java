package com.salayo.locallifebackend.domain.magazine.repository;

import com.salayo.locallifebackend.domain.magazine.entity.MagazinePreviewToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MagazinePreviewTokenRepository extends JpaRepository<MagazinePreviewToken, Long> {

    Optional<MagazinePreviewToken> findByToken(String token);

}
