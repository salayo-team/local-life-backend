package com.salayo.locallifebackend.domain.magazine.service;

import com.salayo.locallifebackend.domain.magazine.dto.MagazineRevisionResponseDto;
import com.salayo.locallifebackend.domain.magazine.entity.Magazine;
import com.salayo.locallifebackend.domain.magazine.entity.MagazineRevision;
import com.salayo.locallifebackend.domain.magazine.repository.MagazineRepository;
import com.salayo.locallifebackend.domain.magazine.repository.MagazineRevisionRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MagazineRevisionService {

    private final MagazineRevisionRepository revisionRepository;
    private final MagazineRepository magazineRepository;

    public MagazineRevisionService(MagazineRevisionRepository revisionRepository, MagazineRepository magazineRepository) {
        this.revisionRepository = revisionRepository;
        this.magazineRepository = magazineRepository;
    }

    @Transactional(readOnly = true)
    public List<MagazineRevisionResponseDto> getRevisions(Long magazineId) {
        Magazine magazine = magazineRepository.findById(magazineId)
            .orElseThrow(() -> new CustomException(ErrorCode.MAGAZINE_NOT_FOUND));

        List<MagazineRevision> revisions = revisionRepository.findAllByMagazineIdOrderByRevisionNumberAsc(magazine.getId());

        return revisions.stream()
            .map(MagazineRevisionResponseDto::new)
            .collect(Collectors.toList());
    }

}
