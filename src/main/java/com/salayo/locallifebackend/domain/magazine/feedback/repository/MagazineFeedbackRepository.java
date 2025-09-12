package com.salayo.locallifebackend.domain.magazine.feedback.repository;

import com.salayo.locallifebackend.domain.magazine.feedback.entity.MagazineFeedback;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MagazineFeedbackRepository extends JpaRepository<MagazineFeedback, Long> {

    List<MagazineFeedback> findByMagazineIdAndLocalCreatorId(Long magazineId, Long localCreatorId);

    Optional<MagazineFeedback> findTopByMagazineIdOrderByCreatedAtDesc(Long magazineId);
}
