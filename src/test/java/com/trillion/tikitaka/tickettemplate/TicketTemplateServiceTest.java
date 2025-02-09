//package com.trillion.tikitaka.tickettemplate;
//
//import com.trillion.tikitaka.tickettemplate.application.TicketTemplateService;
//import com.trillion.tikitaka.tickettemplate.domain.TicketTemplate;
//import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
//import com.trillion.tikitaka.tickettemplate.exception.TicketTemplateInvalidFKException;
//import com.trillion.tikitaka.tickettemplate.exception.TicketTemplateNotFoundException;
//import com.trillion.tikitaka.tickettemplate.infrastructure.TicketTemplateRepository;
//import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
//import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
//import com.trillion.tikitaka.user.domain.User;
//import com.trillion.tikitaka.user.infrastructure.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.BDDMockito.*;
//
//// JUnit5 + Mockito
//@MockitoSettings(strictness = Strictness.LENIENT)
//class TicketTemplateServiceTest {
//
//    @Mock
//    private TicketTemplateRepository templateRepository;
//    @Mock
//    private TicketTypeRepository typeRepository;
//    @Mock
//    private CategoryRepository categoryRepository;
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private TicketTemplateService templateService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    // ---------------------------
//    // CREATE TEST
//    // ---------------------------
//
//    @Test
//    @DisplayName("createTicketTemplate 성공 시 ID 반환")
//    void createTicketTemplate_success() {
//        // given
//        TicketTemplateRequest req = new TicketTemplateRequest();
//        req.setTemplateTitle("컨테이너화 템플릿");
//        req.setTitle("마이크로서비스...");
//        req.setDescription("상세 내용");
//        req.setTypeId(1L);
//        req.setFirstCategoryId(2L);
//        req.setSecondCategoryId(3L);
//        req.setRequesterId(100L);
//        req.setManagerId(101L);
//
//        // FK 유효 -> mocking
//        given(typeRepository.existsById(1L)).willReturn(true);
//        given(categoryRepository.existsById(2L)).willReturn(true);
//        given(categoryRepository.existsById(3L)).willReturn(true);
//        given(userRepository.existsById(100L)).willReturn(true);
//
//        // User manager optional
//        given(userRepository.existsById(101L)).willReturn(true);
//
//        // template save mock
//        TicketTemplate saved = TicketTemplate.builder()
//                .templateTitle("컨테이너화 템플릿")
//                .build();
//        given(templateRepository.save(any(TicketTemplate.class))).willReturn(saved);
//
//        // when
//        Long resultId = templateService.createTicketTemplate(req);
//
//        // then
//        assertNotNull(resultId); // 모의 상황이므로 null 아님을 가정
//        then(templateRepository).should(times(1)).save(any(TicketTemplate.class));
//    }
//
//    @Test
//    @DisplayName("createTicketTemplate - FK Invalid 시 TicketTemplateInvalidFKException 발생")
//    void createTicketTemplate_fkInvalid() {
//        // given
//        TicketTemplateRequest req = new TicketTemplateRequest();
//        req.setTemplateTitle("컨테이너화 템플릿");
//        req.setTypeId(999L);
//        // mocking
//        given(typeRepository.existsById(999L)).willReturn(false);
//
//        // when & then
//        assertThrows(TicketTemplateInvalidFKException.class, () -> {
//            templateService.createTicketTemplate(req);
//        });
//    }
//
//    // ---------------------------
//    // UPDATE TEST
//    // ---------------------------
//
//    @Test
//    @DisplayName("updateTicketTemplate - 성공 시 예외 발생 안 함")
//    void updateTicketTemplate_success() {
//        // given
//        Long templateId = 1L;
//        TicketTemplateRequest req = new TicketTemplateRequest();
//        req.setTemplateTitle("수정 템플릿");
//        req.setTypeId(1L);
//        req.setFirstCategoryId(2L);
//        req.setSecondCategoryId(3L);
//        req.setRequesterId(100L);
//
//        // mocking
//        // 엔티티 기존 값
//        TicketTemplate existing = TicketTemplate.builder()
//                .templateTitle("이전 템플릿")
//                .build();
//        given(templateRepository.findById(templateId)).willReturn(Optional.of(existing));
//
//        given(typeRepository.existsById(1L)).willReturn(true);
//        given(categoryRepository.existsById(2L)).willReturn(true);
//        given(categoryRepository.existsById(3L)).willReturn(true);
//        given(userRepository.existsById(100L)).willReturn(true);
//
//        // when
//        templateService.updateTicketTemplate(templateId, req);
//
//        // then
//        // 정상수행 시 예외 없음
//        then(templateRepository).should(times(1)).findById(templateId);
//        then(templateRepository).should(never()).save(any());
//        // (update 시 save 안 해도 flush로 반영. or JPA might call save, depends on impl)
//    }
//
//    @Test
//    @DisplayName("updateTicketTemplate - NotFound 시 TicketTemplateNotFoundException")
//    void updateTicketTemplate_notFound() {
//        // given
//        Long templateId = 999L;
//        TicketTemplateRequest req = new TicketTemplateRequest();
//        req.setTypeId(1L);
//        // mocking
//        given(templateRepository.findById(templateId)).willReturn(Optional.empty());
//
//        // when & then
//        assertThrows(TicketTemplateNotFoundException.class, () -> {
//            templateService.updateTicketTemplate(templateId, req);
//        });
//    }
//
//    @Test
//    @DisplayName("updateTicketTemplate - FK Invalid 시 예외")
//    void updateTicketTemplate_fkInvalid() {
//        // given
//        Long templateId = 1L;
//        TicketTemplateRequest req = new TicketTemplateRequest();
//        req.setTypeId(999L);
//
//        TicketTemplate existing = TicketTemplate.builder().build();
//        given(templateRepository.findById(templateId)).willReturn(Optional.of(existing));
//        // typeId=999 -> false
//        given(typeRepository.existsById(999L)).willReturn(false);
//
//        // when & then
//        assertThrows(TicketTemplateInvalidFKException.class, () -> {
//            templateService.updateTicketTemplate(templateId, req);
//        });
//    }
//
//    // ---------------------------
//    // DELETE TEST
//    // ---------------------------
//
//    @Test
//    @DisplayName("deleteTicketTemplate - 성공")
//    void deleteTicketTemplate_success() {
//        // given
//        Long templateId = 1L;
//        TicketTemplate existing = TicketTemplate.builder().build();
//        given(templateRepository.findById(templateId)).willReturn(Optional.of(existing));
//
//        // when
//        templateService.deleteTicketTemplate(templateId);
//
//        // then
//        then(templateRepository).should(times(1)).delete(existing);
//    }
//
//    @Test
//    @DisplayName("deleteTicketTemplate - Not Found")
//    void deleteTicketTemplate_notFound() {
//        // given
//        Long templateId = 999L;
//        given(templateRepository.findById(templateId)).willReturn(Optional.empty());
//
//        // when & then
//        assertThrows(TicketTemplateNotFoundException.class, () -> {
//            templateService.deleteTicketTemplate(templateId);
//        });
//    }
//}
