package com.trillion.tikitaka.tickettemplate;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.tickettemplate.application.TicketTemplateService;
import com.trillion.tikitaka.tickettemplate.domain.TicketTemplate;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
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
import org.junit.jupiter.api.Nested;
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

    @InjectMocks
    private TicketTemplateService templateService;

    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // userDetails: id=100
        User mockUser = new User(100L, "testUser", "USER");
        mockUserDetails = new CustomUserDetails(mockUser);
    }

    private TicketTemplateRequest createTicketTemplateRequest(Long typeId,
                                                              Long firstCatId,
                                                              Long secondCatId) {
        return new TicketTemplateRequest(
                typeId,
                firstCatId,
                secondCatId,
                101L,
                "Sample Template Title",
                "Sample Title",
                "Sample Description"
        );
    }

    // ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("생성 관련 테스트")
    class CreateTests {

        @Test
        @DisplayName("[성공] 카테고리 계층 검증 성공 시 티켓 템플릿 정상적으로 생성")
        void createTicketTemplate_success() {
            TicketTemplateRequest req = createTicketTemplateRequest(1L, 2L, 3L);

            TicketType mockType = mock(TicketType.class);
            Category mockFirst = mock(Category.class);
            Category mockSecond = mock(Category.class);
            User mockRequester = mock(User.class);
            User mockManager = mock(User.class);

            when(typeRepository.findById(1L)).thenReturn(Optional.of(mockType));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(mockFirst));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(mockSecond));

            when(mockSecond.getParent()).thenReturn(mockFirst);

            when(userRepository.findById(100L)).thenReturn(Optional.of(mockRequester));
            when(userRepository.findById(101L)).thenReturn(Optional.of(mockManager));

            when(templateRepository.save(any(TicketTemplate.class)))
                    .thenAnswer(i -> i.getArgument(0));

            assertDoesNotThrow(() -> templateService.createTicketTemplate(req, mockUserDetails));
            verify(templateRepository, times(1)).save(any(TicketTemplate.class));
        }

        @Test
        @DisplayName("[예외 발생] 존재하지 않는 FK 참조해서 생성")
        void createTicketTemplate_fkInvalid() {
            TicketTemplateRequest req = createTicketTemplateRequest(999L, 2L, 3L);

            when(typeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(TicketTemplateInvalidFKException.class,
                    () -> templateService.createTicketTemplate(req, mockUserDetails));
        }

        @Test
        @DisplayName("[예외 발생] 2차 카테고리가 1차 카테고리에 속하지 않음 (카테고리 계층 불일치)")
        void createTicketTemplate_categoryMismatch() {
            TicketTemplateRequest req = createTicketTemplateRequest(1L, 2L, 3L);

            TicketType mockTicketType = mock(TicketType.class);
            Category mockFirstCategory = mock(Category.class);
            Category mockSecondCategory = mock(Category.class);
            Category mockParentOfSecond = mock(Category.class);

            User mockRequester = mock(User.class);
            User mockManager = mock(User.class);

            when(typeRepository.findById(1L)).thenReturn(Optional.of(mockTicketType));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(mockFirstCategory));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(mockSecondCategory));

            when(mockSecondCategory.getParent()).thenReturn(mockParentOfSecond);


            when(mockFirstCategory.getId()).thenReturn(2L);

            when(mockParentOfSecond.getId()).thenReturn(999L);


            when(userRepository.findById(100L)).thenReturn(Optional.of(mockRequester));
            when(userRepository.findById(101L)).thenReturn(Optional.of(mockManager));


            when(mockRequester.getId()).thenReturn(100L);


            when(templateRepository.save(any(TicketTemplate.class)))
                    .thenAnswer(i -> i.getArgument(0));


            assertThrows(TicketTemplateInvalidFKException.class,
                    () -> templateService.createTicketTemplate(req, mockUserDetails));


            verify(templateRepository, never()).save(any(TicketTemplate.class));

        }

    }

    // ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("수정 관련 테스트")
    class UpdateTests {

        @Test
        @DisplayName("[예외 발생] 존재하지 않는 템플릿 ID로 수정")
        void updateTicketTemplate_notFound() {
            TicketTemplateRequest req = createTicketTemplateRequest(1L, 2L, 3L);
            when(templateRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(TicketTemplateNotFoundException.class,
                    () -> templateService.updateTicketTemplate(1L, req, mockUserDetails));
        }

        @Test
        @DisplayName("[성공] 정상적인 방법으로 템플릿 수정")
        void updateTicketTemplate_success() {
            TicketTemplateRequest req = createTicketTemplateRequest(1L, 2L, 3L);

            TicketTemplate mockTemplate = mock(TicketTemplate.class);
            User mockTemplateRequester = mock(User.class);

            when(mockTemplateRequester.getId()).thenReturn(100L);
            when(mockTemplate.getRequester()).thenReturn(mockTemplateRequester);

            when(templateRepository.findById(1L)).thenReturn(Optional.of(mockTemplate));

            TicketType mockType = mock(TicketType.class);
            Category mockFirst = mock(Category.class);
            Category mockSecond = mock(Category.class);
            User mockManager = mock(User.class);

            when(typeRepository.findById(1L)).thenReturn(Optional.of(mockType));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(mockFirst));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(mockSecond));

            when(mockSecond.getParent()).thenReturn(mockFirst);

            when(userRepository.findById(100L)).thenReturn(Optional.of(mockTemplateRequester));
            when(userRepository.findById(101L)).thenReturn(Optional.of(mockManager));

            when(templateRepository.save(any(TicketTemplate.class)))
                    .thenAnswer(i -> i.getArgument(0));

            assertDoesNotThrow(() -> templateService.updateTicketTemplate(1L, req, mockUserDetails));

            verify(mockTemplate, times(1)).update(
                    eq(req.getTemplateTitle()),
                    eq(req.getTitle()),
                    eq(req.getDescription()),
                    eq(mockType),
                    eq(mockFirst),
                    eq(mockSecond),
                    eq(mockTemplateRequester),
                    eq(mockManager)
            );
        }

        @Test
        @DisplayName("[예외 발생] 2차 카테고리가 1차 카테고리에 속하지 않으면서 수정")
        void updateTicketTemplate_categoryMismatch() {
            TicketTemplateRequest req = createTicketTemplateRequest(1L, 2L, 3L);

            TicketTemplate mockTemplate = mock(TicketTemplate.class);
            User mockTemplateRequester = mock(User.class);
            when(mockTemplateRequester.getId()).thenReturn(100L);
            when(mockTemplate.getRequester()).thenReturn(mockTemplateRequester);

            when(templateRepository.findById(1L)).thenReturn(Optional.of(mockTemplate));

            TicketType mockType = mock(TicketType.class);
            Category mockFirst = mock(Category.class);
            Category mockSecond = mock(Category.class);

            when(typeRepository.findById(1L)).thenReturn(Optional.of(mockType));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(mockFirst));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(mockSecond));


            Category differentParent = mock(Category.class);
            when(differentParent.getId()).thenReturn(999L);
            when(mockSecond.getParent()).thenReturn(differentParent);

            User mockManager = mock(User.class);
            User mockRequester = mock(User.class); // userDetails=100
            when(mockRequester.getId()).thenReturn(100L);
            when(userRepository.findById(100L)).thenReturn(Optional.of(mockRequester));
            when(userRepository.findById(101L)).thenReturn(Optional.of(mockManager));

            when(templateRepository.save(any(TicketTemplate.class)))
                    .thenAnswer(i -> i.getArgument(0));

            assertThrows(TicketTemplateInvalidFKException.class,
                    () -> templateService.updateTicketTemplate(1L, req, mockUserDetails));

            verify(mockTemplate, never()).update(any(), any(), any(), any(), any(), any(), any(), any());
        }
    }

    // ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("조회 관련 테스트")
    class RetrieveTests {

        @Test
        @DisplayName("[예외 발생] 존재하지 않는 템플릿 ID로 조회")
        void getOneTicketTemplate_notFound() {
            when(templateRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(TicketTemplateNotFoundException.class,
                    () -> templateService.getOneTicketTemplate(1L, 100L));
        }

        @Test
        @DisplayName("[성공] 정상적으로 본인 TicketTemplate 목록 조회")
        void getMyTemplates_success() {
            TicketTemplateListResponse listResponse = mock(TicketTemplateListResponse.class);
            List<TicketTemplateListResponse> mockResponseList = List.of(listResponse);

            when(templateRepository.getAllTemplates(100L)).thenReturn(mockResponseList);

            List<TicketTemplateListResponse> responses =
                    templateService.getMyTemplates(mockUserDetails);

            assertNotNull(responses);
            assertFalse(responses.isEmpty());
        }
    }

    // ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("❌ 삭제 관련 테스트")
    class DeleteTests {

        @Test
        @DisplayName("[예외 발생] 존재하지 않는 템플릿 ID로 삭제")
        void deleteTicketTemplate_notFound() {
            when(templateRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(TicketTemplateNotFoundException.class,
                    () -> templateService.deleteTicketTemplate(1L, mockUserDetails));
        }

        @Test
        @DisplayName("[예외 발생] 권한 없는 유저가 티켓 템플릿 삭제")
        void deleteTicketTemplate_accessDenied() {
            TicketTemplate template = mock(TicketTemplate.class);
            User differentRequester = mock(User.class);

            when(differentRequester.getId()).thenReturn(200L);
            when(template.getRequester()).thenReturn(differentRequester);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

            assertThrows(CustomException.class,
                    () -> templateService.deleteTicketTemplate(1L, mockUserDetails));
        }

        @Test
        @DisplayName("[성공] 템플릿 삭제")
        void deleteTicketTemplate_success() {
            TicketTemplate template = mock(TicketTemplate.class);
            User sameRequester = mock(User.class);

            when(sameRequester.getId()).thenReturn(100L);
            when(template.getRequester()).thenReturn(sameRequester);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

            assertDoesNotThrow(() -> templateService.deleteTicketTemplate(1L, mockUserDetails));
            verify(templateRepository, times(1)).delete(template);
        }
    }
}
