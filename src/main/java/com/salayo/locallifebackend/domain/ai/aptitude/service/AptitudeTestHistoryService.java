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

    // 테스트 이력 저장
    @Transactional
    public AptitudeTestHistory saveTestHistory(Member member, AptitudeTestHistory history) {
        AptitudeTestHistory saved = aptitudeTestHistoryRepository.save(history);
        log.info("[이력 저장] Step {} 저장 완료 - sessionId: {}, aptitudeType: {}",
            history.getStep(), history.getSessionId(), history.getAnalyzedAptitudeType());
        return saved;
    }

    // 세션별 이력 조회
    @Transactional(readOnly = true)
    public List<AptitudeTestHistoryResponseDto> getTestHistoryBySession(Long memberId, String sessionId) {
        Member member = memberRepository.findActiveByIdOrThrow(memberId);
        
        List<AptitudeTestHistory> histories = aptitudeTestHistoryRepository
            .findBySessionIdAndMemberOrderByStepAsc(sessionId, member);
        
        // 세션이 존재하지 않거나, 이력이 없는 경우
        if (histories.isEmpty()) {
            throw new CustomException(ErrorCode.SESSION_NOT_FOUND);
        }

        return histories.stream()
            .map(this::convertToDto)
            .toList();
    }

    // 완료된 테스트 이력 조회
    @Transactional(readOnly = true)
    public List<AptitudeTestHistoryResponseDto> getCompletedTestHistory(Long memberId) {
        Member member = memberRepository.findActiveByIdOrThrow(memberId);
        
        return aptitudeTestHistoryRepository
            .findByMemberAndIsCompletedTrueOrderByCreatedAtDesc(member)
            .stream()
            .map(this::convertToDto)
            .toList();
    }

    // 세션 완료 처리
    @Transactional
    public void markSessionAsCompleted(String sessionId) {
        aptitudeTestHistoryRepository.markSessionAsCompleted(sessionId);
        log.info("[테스트 완료] 세션 {} 완료 처리", sessionId);
    }

    // 미완료 테스트 삭제
    @Transactional
    public void deleteIncompleteTests(Member member) {
        aptitudeTestHistoryRepository.deleteByMemberAndIsCompletedFalse(member);
        log.info("[테스트 정리] 미완료 테스트 삭제 - memberId: {}", member.getId());
    }

    // 세션의 이력 조회 (이어하기용)
    @Transactional(readOnly = true)
    public List<AptitudeTestHistory> getSessionHistories(String sessionId, Member member) {
        return aptitudeTestHistoryRepository.findBySessionIdAndMemberOrderByStepAsc(sessionId, member);
    }

    // Entity -> DTO 변환
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


