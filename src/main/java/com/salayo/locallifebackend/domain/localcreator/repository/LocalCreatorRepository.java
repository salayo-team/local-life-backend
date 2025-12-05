package com.salayo.locallifebackend.domain.localcreator.repository;

import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import com.salayo.locallifebackend.domain.localcreator.enums.CreatorStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocalCreatorRepository extends JpaRepository<LocalCreator, Long> {

    Optional<LocalCreator> findByMemberId(Long memberId);

    Optional<LocalCreator> findByMemberIdAndCreatorStatus(Long memberId, CreatorStatus status);

    List<LocalCreator> findAllByCreatorStatus(CreatorStatus creatorStatus);

    default LocalCreator findByIdOrThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.LOCAL_CREATOR_NOT_FOUND));
    }

    default LocalCreator findByMemberIdOrThrow(Long memberId) {
        return findByMemberId(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.LOCAL_CREATOR_NOT_FOUND));
    }

    @Query("SELECT lc FROM LocalCreator lc JOIN FETCH lc.member m WHERE m.email = :email")
    Optional<LocalCreator> findByMemberEmail(@Param("email") String email);

    boolean existsByMember_PhoneNumberAndMember_IdNot(String phoneNumber, Long id);

    @Query("""
        SELECT lc FROM LocalCreator lc
        JOIN lc.member m
        WHERE lc.creatorStatus = :status
        AND m.deletedStatus = :deletedStatus
        """)
    List<LocalCreator> findAllActiveByCreatorStatus(@Param("status") CreatorStatus creatorStatus,
        @Param("deletedStatus") DeletedStatus deletedStatus);

}
