package com.salayo.locallifebackend.domain.admin.service;

import com.salayo.locallifebackend.domain.admin.dto.CreatorPendingResponseDto;
import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import com.salayo.locallifebackend.domain.localcreator.enums.CreatorStatus;
import com.salayo.locallifebackend.domain.localcreator.repository.LocalCreatorRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final LocalCreatorRepository localCreatorRepository;

    public AdminService(LocalCreatorRepository localCreatorRepository) {
        this.localCreatorRepository = localCreatorRepository;
    }

    public List<CreatorPendingResponseDto> getPendingCreators() {
        return localCreatorRepository.findAllByCreatorStatus(CreatorStatus.PENDING).stream()
            .map(CreatorPendingResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public void approveCreator(Long creatorId) {
        LocalCreator localCreator = localCreatorRepository.findById(creatorId)
            .orElseThrow(() -> new CustomException(ErrorCode.LOCALCREATOR_NOT_FOUND));

        if (localCreator.getCreatorStatus() != CreatorStatus.PENDING) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_LOCALCREATOR);
        }

        localCreator.approve();
    }

}
