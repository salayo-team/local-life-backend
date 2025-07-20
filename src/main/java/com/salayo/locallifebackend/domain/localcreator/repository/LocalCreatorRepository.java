package com.salayo.locallifebackend.domain.localcreator.repository;

import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import com.salayo.locallifebackend.domain.localcreator.enums.CreatorStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalCreatorRepository extends JpaRepository<LocalCreator, Long> {

    Optional<LocalCreator> findByMemberId(Long memberId);

    Optional<LocalCreator> findByMemberIdAndCreatorStatus(Long memberId, CreatorStatus status);

    List<LocalCreator> findAllByCreatorStatus(CreatorStatus creatorStatus);

    default LocalCreator findByIdOrThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.LOCALCREATOR_NOT_FOUND));
    }

}
