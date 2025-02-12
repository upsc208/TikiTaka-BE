package com.trillion.tikitaka.tickettemplate;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.tickettemplate.application.TicketTemplateService;
import com.trillion.tikitaka.tickettemplate.domain.TicketTemplate;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateResponse;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateListResponse;
import com.trillion.tikitaka.tickettemplate.exception.TicketTemplateInvalidFKException;
import com.trillion.tikitaka.tickettemplate.exception.TicketTemplateNotFoundException;
import com.trillion.tikitaka.tickettemplate.infrastructure.TicketTemplateRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TicketTemplateServiceTest {
    @Mock private TicketTemplateRepository templateRepository;
    @Mock private TicketTypeRepository typeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private TicketTemplateService templateService;
    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        User mockUser = new User(100L, "testUser", "USER");
        mockUserDetails = new CustomUserDetails(mockUser);
    }

    private TicketTemplateRequest createTicketTemplateRequest(Long typeId, Long firstCategoryId, Long secondCategoryId) {
        return new TicketTemplateRequest(typeId, firstCategoryId, secondCategoryId, 101L,
                "Sample Template", "Sample Title", "Sample Description");
    }

    @Test
    @DisplayName("createTicketTemplate - 성공 (카테고리 계층 검증 포함)")
    void createTicketTemplate_success() {
        TicketTemplateRequest req = createTicketTemplateRequest(1L, 2L, 3L);
        TicketType mockTicketType = mock(TicketType.class);
        Category mockFirstCategory = mock(Category.class);
        Category mockSecondCategory = mock(Category.class);
        User mockRequester = mock(User.class);
        User mockManager = mock(User.class);

        when(mockRequester.getId()).thenReturn(100L);
        when(mockManager.getId()).thenReturn(101L);
        when(typeRepository.findById(1L)).thenReturn(Optional.of(mockTicketType));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(mockFirstCategory));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(mockSecondCategory));
        when(userRepository.findById(100L)).thenReturn(Optional.of(mockRequester));
        when(userRepository.findById(101L)).thenReturn(Optional.of(mockManager));
        when(mockSecondCategory.getParent()).thenReturn(mockFirstCategory);

        when(templateRepository.save(any(TicketTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> templateService.createTicketTemplate(req, mockUserDetails));
        verify(templateRepository, times(1)).save(any(TicketTemplate.class));
    }

    @Test
    @DisplayName("createTicketTemplate - FK Invalid 시 예외 발생")
    void createTicketTemplate_fkInvalid() {
        TicketTemplateRequest req = createTicketTemplateRequest(999L, 2L, 3L);
        when(typeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(TicketTemplateInvalidFKException.class, () -> templateService.createTicketTemplate(req, mockUserDetails));
    }

    @Test
    @DisplayName("updateTicketTemplate - 존재하지 않는 템플릿 예외")
    void updateTicketTemplate_notFound() {
        TicketTemplateRequest req = createTicketTemplateRequest(1L, 2L, 3L);
        when(templateRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TicketTemplateNotFoundException.class, () -> templateService.updateTicketTemplate(1L, req, mockUserDetails));
    }

    @Test
    @DisplayName("deleteTicketTemplate - 존재하지 않는 템플릿 삭제 예외")
    void deleteTicketTemplate_notFound() {
        when(templateRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TicketTemplateNotFoundException.class, () -> templateService.deleteTicketTemplate(1L, mockUserDetails));
    }

    @Test
    @DisplayName("deleteTicketTemplate - 권한 없음 예외")
    void deleteTicketTemplate_accessDenied() {
        TicketTemplate template = mock(TicketTemplate.class);
        User differentRequester = mock(User.class);
        when(differentRequester.getId()).thenReturn(200L);
        when(template.getRequester()).thenReturn(differentRequester);
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

        assertThrows(CustomException.class, () -> templateService.deleteTicketTemplate(1L, mockUserDetails));
    }

    @Test
    @DisplayName("getOneTicketTemplate - 존재하지 않는 템플릿 예외")
    void getOneTicketTemplate_notFound() {
        when(templateRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TicketTemplateNotFoundException.class, () -> templateService.getOneTicketTemplate(1L, 100L));
    }

    @Test
    @DisplayName("getMyTemplates - 정상 조회")
    void getMyTemplates_success() {
        TicketTemplateListResponse listResponse = mock(TicketTemplateListResponse.class);
        List<TicketTemplateListResponse> mockResponseList = List.of(listResponse);
        when(templateRepository.getAllTemplates(mockUserDetails.getId())).thenReturn(mockResponseList);
        List<TicketTemplateListResponse> responses = templateService.getMyTemplates(mockUserDetails);
        assertNotNull(responses);
        assertFalse(responses.isEmpty());
    }
}
