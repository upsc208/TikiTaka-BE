package com.trillion.tikitaka.inquiry.application;

import com.trillion.tikitaka.inquiry.domain.Inquiry;
import com.trillion.tikitaka.inquiry.dto.request.InquiryRequest;
import com.trillion.tikitaka.inquiry.dto.response.InquiryResponse;
import com.trillion.tikitaka.inquiry.infrastructure.InquiryRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquiryResponse createInquiry(Long userId, InquiryRequest request) {
        User writer = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Inquiry inquiry = Inquiry.builder()
                .content(request.getContent())
                .writer(writer)
                .build();

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return new InquiryResponse(savedInquiry);
    }
}
