package com.trillion.tikitaka.inquiry.infrastructure;

import com.trillion.tikitaka.inquiry.domain.Inquiry;
import com.trillion.tikitaka.inquiry.dto.response.InquiryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Page<Inquiry> findByRequester_Id(Long requesterId, Pageable pageable);
}
