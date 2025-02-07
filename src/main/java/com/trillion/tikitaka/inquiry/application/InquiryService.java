package com.trillion.tikitaka.inquiry.application;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.inquiry.domain.Inquiry;
import com.trillion.tikitaka.inquiry.dto.request.InquiryRequest;
import com.trillion.tikitaka.inquiry.dto.response.InquiryResponse;
import com.trillion.tikitaka.inquiry.infrastructure.InquiryRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquiryResponse createInquiry(Long userId, InquiryRequest request) {
        log.info("[문의사항 작성]");
        User writer = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Inquiry inquiry = new Inquiry(writer, request.getType(), request.getTitle(), request.getContent());
        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return new InquiryResponse(savedInquiry);
    }

    public Page<InquiryResponse> getAllInquiries(Pageable pageable, CustomUserDetails userDetails) {
        log.info("[문의사항 전체 조회] 요청자: {}, 역할: {}", userDetails.getUser().getUsername(), userDetails.getUser().getRole());

        if (userDetails.getUser().getRole() == Role.ADMIN) {
            return inquiryRepository.findAll(pageable).map(InquiryResponse::new);
        } else {
            return inquiryRepository.findByRequester_Id(userDetails.getId(), pageable).map(InquiryResponse::new);
        }

    }

    @Transactional
    public void answerInquiry(Long inquiryId, String answer) {
        log.info("[문의사항 답변 작성]");
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));

        if (inquiry.getAnswer() != null) {
            log.error("[문의사항 답변 작성 실패] 이미 답변이 작성된 문의사항");
            throw new CustomException(ErrorCode.INQUIRY_ALREADY_ANSWERED);
        }

        inquiry.updateAnswer(answer);
        inquiryRepository.save(inquiry);
    }


}

