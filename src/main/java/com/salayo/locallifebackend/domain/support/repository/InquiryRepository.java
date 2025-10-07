package com.salayo.locallifebackend.domain.support.repository;

import com.salayo.locallifebackend.domain.support.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

}
