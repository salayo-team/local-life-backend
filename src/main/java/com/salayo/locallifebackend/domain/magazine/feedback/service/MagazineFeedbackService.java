package com.salayo.locallifebackend.domain.magazine.feedback.service;

import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import com.salayo.locallifebackend.domain.localcreator.repository.LocalCreatorRepository;
import com.salayo.locallifebackend.domain.magazine.entity.Magazine;
import com.salayo.locallifebackend.domain.magazine.feedback.dto.MagazineFeedbackCreateRequestDto;
import com.salayo.locallifebackend.domain.magazine.feedback.dto.MagazineFeedbackResponseDto;
import com.salayo.locallifebackend.domain.magazine.feedback.entity.MagazineFeedback;
import com.salayo.locallifebackend.domain.magazine.feedback.repository.MagazineFeedbackRepository;
import com.salayo.locallifebackend.domain.magazine.repository.MagazineRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MagazineFeedbackService {

    private final MagazineFeedbackRepository magazineFeedbackRepository;
    private final MagazineRepository magazineRepository;
    private final LocalCreatorRepository localCreatorRepository;

    public MagazineFeedbackService(MagazineFeedbackRepository magazineFeedbackRepository, MagazineRepository magazineRepository,
        LocalCreatorRepository localCreatorRepository) {
        this.magazineFeedbackRepository = magazineFeedbackRepository;
        this.magazineRepository = magazineRepository;
        this.localCreatorRepository = localCreatorRepository;
    }

    @Transactional
    public void createFeedback(Long magazineId, Long memberId, MagazineFeedbackCreateRequestDto createRequestDto) {
        Magazine magazine = magazineRepository.findById(magazineId)
            .orElseThrow(() -> new CustomException(ErrorCode.MAGAZINE_NOT_FOUND));

        LocalCreator localCreator = localCreatorRepository.findByMemberId(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.LOCAL_CREATOR_NOT_FOUND));

        if (!magazine.getLocalCreator().getId().equals(localCreator.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        magazineFeedbackRepository.findTopByMagazineIdAndLocalCreatorIdOrderByCreatedAtDesc(magazineId, localCreator.getId())
            .ifPresent(lastFeedback -> {
                if (!lastFeedback.isReflected()) {
                    throw new CustomException(ErrorCode.PREVIOUS_FEEDBACK_NOT_REFLECTED);
                }
            });

        int feedbackCount = magazineFeedbackRepository.countByMagazineIdAndLocalCreatorId(magazineId, localCreator.getId());

        if (feedbackCount >= 3) {
            throw new CustomException(ErrorCode.FEEDBACK_LIMIT_EXCEEDED);
        }

        MagazineFeedback magazineFeedback = MagazineFeedback.builder()
            .magazine(magazine)
            .localCreator(localCreator)
            .content(createRequestDto.getContent())
            .revisionCount(feedbackCount + 1)
            .build();

        magazineFeedbackRepository.save(magazineFeedback);
    }

    @Transactional
    public List<MagazineFeedbackResponseDto> getMyFeedbacks(Long magazineId, Long memberId) {
        LocalCreator localCreator = localCreatorRepository.findByMemberId(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.LOCAL_CREATOR_NOT_FOUND));

        Magazine magazine = magazineRepository.findById(magazineId)
            .orElseThrow(() -> new CustomException(ErrorCode.MAGAZINE_NOT_FOUND));

        if (!magazine.getLocalCreator().getId().equals(localCreator.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        return magazineFeedbackRepository.findByMagazineIdAndLocalCreatorId(magazineId, localCreator.getId())
            .stream()
            .map(MagazineFeedbackResponseDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MagazineFeedbackResponseDto> getFeedbacksForAdmin(Long magazineId) {
        magazineRepository.findById(magazineId).orElseThrow(() -> new CustomException(ErrorCode.MAGAZINE_NOT_FOUND));

        return magazineFeedbackRepository.findByMagazineId(magazineId)
            .stream()
            .map(MagazineFeedbackResponseDto::new)
            .collect(Collectors.toList());
    }
}