package com.salayo.locallifebackend.domain.admin.service;

import com.salayo.locallifebackend.domain.admin.dto.CreatorPendingResponseDto;
import com.salayo.locallifebackend.domain.localcreator.enums.CreatorStatus;
import com.salayo.locallifebackend.domain.localcreator.repository.LocalCreatorRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

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

}
