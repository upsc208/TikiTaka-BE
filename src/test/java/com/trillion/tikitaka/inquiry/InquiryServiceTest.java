package com.trillion.tikitaka.inquiry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.inquiry.application.InquiryService;
import com.trillion.tikitaka.inquiry.domain.Inquiry;
import com.trillion.tikitaka.inquiry.domain.InquiryType;
import com.trillion.tikitaka.inquiry.dto.request.InquiryRequest;
import com.trillion.tikitaka.inquiry.dto.response.InquiryResponse;
import com.trillion.tikitaka.inquiry.infrastructure.InquiryRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("문의 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    private InquiryService inquiryService;
    private InquiryRepository inquiryRepository;
    private UserRepository userRepository;

    private User user;
    @Mock
    private CustomUserDetails userDetails;
    private Inquiry inquiry;

    @BeforeEach
    void setUp() {
        inquiryRepository = mock(InquiryRepository.class);
        userRepository = mock(UserRepository.class);
        inquiryService = new InquiryService(inquiryRepository, userRepository);

        user = new User(1L, "testUser", "MANAGER");
        this.userDetails = new CustomUserDetails(this.user);

        inquiry = new Inquiry(1L, user,InquiryType.QUESTION,"Test Title","Test Content",null,false);
    }

    @Test
    @DisplayName("문의사항 생성 시 정상적으로 저장된다.")
    void should_SaveInquiry_When_CreatingInquiry() {
        InquiryRequest request = new InquiryRequest("New Inquiry", "Some content", InquiryType.QUESTION);


        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(inquiry);

        InquiryResponse response = inquiryService.createInquiry(user.getId(), request);
        assertThat(response.getTitle()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 문의사항을 작성하면 예외가 발생한다.")
    void should_ThrowException_When_UserNotFound() {
        InquiryRequest request = new InquiryRequest();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> inquiryService.createInquiry(999L, request));
    }

    @Test
    @DisplayName("문의사항 전체 조회 시 페이징된 목록이 반환된다.")
    void should_ReturnPagedInquiries_When_GetAllInquiries() {
        Pageable pageable = mock(Pageable.class);
        Page<Inquiry> inquiryPage = new PageImpl<>(Collections.singletonList(inquiry));
        when(inquiryRepository.findByRequester_Id(anyLong(), any())).thenReturn(inquiryPage);

        Page<InquiryResponse> result = inquiryService.getAllInquiries(pageable, userDetails);
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("문의사항에 답변을 작성하면 답변이 정상적으로 저장된다.")
    void should_UpdateInquiry_When_Answering() {
        when(inquiryRepository.findById(anyLong())).thenReturn(Optional.of(inquiry));

        inquiryService.answerInquiry(inquiry.getId(), "This is an answer.");

        assertThat(inquiry.getAnswer()).isEqualTo("This is an answer.");
    }

    @Test
    @DisplayName("존재하지 않는 문의사항에 답변을 작성하려 하면 예외가 발생한다.")
    void should_ThrowException_When_InquiryNotFound() {
        when(inquiryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> inquiryService.answerInquiry(999L, "Answer"));
    }
}
