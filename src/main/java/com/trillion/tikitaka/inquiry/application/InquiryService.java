package com.trillion.tikitaka.inquiry.application;

import com.trillion.tikitaka.inquiry.domain.Inquiry;
import com.trillion.tikitaka.inquiry.dto.request.InquiryRequest;
import com.trillion.tikitaka.inquiry.dto.response.InquiryResponse;
import com.trillion.tikitaka.inquiry.infrastructure.InquiryRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquiryResponse createInquiry(Long userId, InquiryRequest request) {
        User writer = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Inquiry inquiry = new Inquiry(writer, request.getType(), request.getTitle(), request.getContent()); // ✅ title 추가
        Inquiry savedInquiry = inquiryRepository.save(inquiry);

        return new InquiryResponse(savedInquiry);
    }


    public List<InquiryResponse> getAllInquiries() {
        return inquiryRepository.findAll().stream()
                .map(InquiryResponse::new)
                .collect(Collectors.toList());
    }
}
