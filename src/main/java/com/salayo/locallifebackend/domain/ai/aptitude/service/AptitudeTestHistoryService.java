package com.salayo.locallifebackend.domain.ai.aptitude.service;

import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestHistoryResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestHistory;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.AptitudeTestHistoryRepository;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AptitudeTestHistoryService {
    
    private final AptitudeTestHistoryRepository aptitudeTestHistoryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public AptitudeTestHistory saveTestHistory(Member member, AptitudeTestHistory history) {
        AptitudeTestHistory saved = aptitudeTestHistoryRepository.save(history);
        log.info("[이력 저장] Step {} 저장 완료 - sessionId: {}, aptitudeType: {}",
            history.getStep(), history.getSessionId(), history.getAnalyzedAptitudeType());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AptitudeTestHistoryResponseDto> getTestHistoryBySession(Long memberId, String sessionId) {
        Member member = memberRepository.findActiveByIdOrThrow(memberId);
        
        List<AptitudeTestHistory> histories = aptitudeTestHistoryRepository
            .findBySessionIdAndMemberOrderByStepAsc(sessionId, member);
        
        if (histories.isEmpty()) {
            throw new CustomException(ErrorCode.SESSION_NOT_FOUND);
        }

        return histories.stream()
            .map(this::convertToDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AptitudeTestHistoryResponseDto> getCompletedTestHistory(Long memberId) {
        Member member = memberRepository.findActiveByIdOrThrow(memberId);
        
        return aptitudeTestHistoryRepository
            .findByMemberAndIsCompletedTrueOrderByCreatedAtDesc(member)
            .stream()
            .map(this::convertToDto)
            .toList();
    }

    @Transactional
    public void markSessionAsCompleted(String sessionId) {
        aptitudeTestHistoryRepository.markSessionAsCompleted(sessionId);
        log.info("[테스트 완료] 세션 {} 완료 처리", sessionId);
    }

    @Transactional
    public void deleteIncompleteTests(Member member) {
        aptitudeTestHistoryRepository.deleteByMemberAndIsCompletedFalse(member);
        log.info("[테스트 정리] 미완료 테스트 삭제 - memberId: {}", member.getId());
    }

    @Transactional(readOnly = true)
    public List<AptitudeTestHistory> getSessionHistories(String sessionId, Member member) {
        return aptitudeTestHistoryRepository.findBySessionIdAndMemberOrderByStepAsc(sessionId, member);
    }

    private AptitudeTestHistoryResponseDto convertToDto(AptitudeTestHistory history) {
        return AptitudeTestHistoryResponseDto.builder()
            .historyId(history.getId())
            .step(history.getStep())
            .questionText(history.getQuestionText())
            .userResponse(history.getUserResponse())
            .aiResponse(history.getAiResponse())
            .aptitudeType(history.getAnalyzedAptitudeType())
            .confidenceScore(history.getConfidenceScore())
            .sessionId(history.getSessionId())
            .isCompleted(history.isCompleted())
            .createdAt(history.getCreatedAt())
            .build();
    }
}


