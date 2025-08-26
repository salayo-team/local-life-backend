package com.salayo.locallifebackend.domain.admin.service;

import static com.salayo.locallifebackend.global.util.CacheKeyPrefix.CREATOR_REAPPLY_TOKEN;

import com.salayo.locallifebackend.domain.admin.dto.CreatorPendingResponseDto;
import com.salayo.locallifebackend.domain.email.service.EmailService;
import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import com.salayo.locallifebackend.domain.localcreator.enums.CreatorStatus;
import com.salayo.locallifebackend.domain.localcreator.repository.LocalCreatorRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final LocalCreatorRepository localCreatorRepository;
    private final EmailService emailService;
    private final RedisTemplate<String, String> creatorReapplyRedisTemplate;

    @Value("${frontend.url}")
    private String frontendUrl;

    public AdminService(LocalCreatorRepository localCreatorRepository, EmailService emailService,
        @Qualifier("creatorReapplyRedisTemplate") RedisTemplate<String, String> creatorReapplyRedisTemplate) {
        this.localCreatorRepository = localCreatorRepository;
        this.emailService = emailService;
        this.creatorReapplyRedisTemplate = creatorReapplyRedisTemplate;
    }

    public List<CreatorPendingResponseDto> getPendingCreators() {
        return localCreatorRepository.findAllByCreatorStatus(CreatorStatus.PENDING).stream()
            .map(CreatorPendingResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public void approveCreator(Long localcreatorId) {
        LocalCreator localCreator = localCreatorRepository.findByIdOrThrow(localcreatorId);

        if (localCreator.getCreatorStatus() != CreatorStatus.PENDING) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_LOCALCREATOR);
        }

        localCreator.approve();
    }

    @Transactional
    public void rejectCreator(Long localcreatorId, String rejectedReason) {
        LocalCreator localCreator = localCreatorRepository.findByIdOrThrow(localcreatorId);

        if (localCreator.getCreatorStatus() != CreatorStatus.PENDING) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_LOCALCREATOR);
        }

        localCreator.reject(rejectedReason);

        String token = UUID.randomUUID().toString();
        creatorReapplyRedisTemplate.opsForValue().set(CREATOR_REAPPLY_TOKEN + localCreator.getId(), token, Duration.ofHours(72));

        String email = localCreator.getMember().getEmail();
        String businessName = localCreator.getBusinessName();
        String link = frontendUrl + "/reapply?token=" + token;

        emailService.sendRejectionMail(email, businessName, rejectedReason, link);
    }

    // TODO : 프론트에서 재제출 버튼을 누르면 재제출 API 호출
    //  1. 토큰 검증 후 거절 -> 대기상태로 바꿔줌
    //  2. 다시 파일 첨부할 수 있도록 함 -> 파일 목적 맞춰서 저장

}
