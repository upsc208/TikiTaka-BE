package com.trillion.tikitaka.ticket;

import com.trillion.tikitaka.attachment.application.FileService;
import com.trillion.tikitaka.attachment.dto.response.AttachmentResponse;
import com.trillion.tikitaka.attachment.infrastructure.AttachmentRepository;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.InvalidCategoryLevelException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.history.application.HistoryService;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.request.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.request.EditCategory;
import com.trillion.tikitaka.ticket.dto.request.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.request.EditTicketRequest;
import com.trillion.tikitaka.ticket.dto.response.TicketListResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketResponse;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.exception.UnauthorizedTicketAccessException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.exception.TicketTypeNotFoundException;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("티켓 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private HistoryService historyService;

    @Mock
    private FileService fileService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TicketService ticketService;

    private CustomUserDetails userDetails;
    private User user;
    private TicketType ticketType;
    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("test.ts")
                .email("test@email.com").
                role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        userDetails = new CustomUserDetails(user);

        ticketType = TicketType.builder().name("생성").build();
        ReflectionTestUtils.setField(ticketType, "id", 10L);

        category1 = Category.builder().name("카테고리1").build();
        ReflectionTestUtils.setField(category1, "id", 20L);

        category2 = Category.builder().name("카테고리2").parent(category1).build();
        ReflectionTestUtils.setField(category2, "id", 30L);
    }

    @Nested
    @DisplayName("티켓 생성 테스트")
    class DescribeCreateTicket {

        @Test
        @DisplayName("유효한 요청이 들어왔을 때 티켓을 생성한다.")
        void should_CreateTicket_When_ValidRequest() {
            // given
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("테스트 티켓")
                    .description("티켓 설명")
                    .typeId(10L)
                    .firstCategoryId(20L)
                    .secondCategoryId(30L)
                    .deadline(LocalDateTime.now().plusDays(3))
                    .urgent(false)
                    .build();
            List<MultipartFile> files = Collections.emptyList();

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(ticketTypeRepository.findById(10L)).thenReturn(Optional.of(ticketType));
            when(categoryRepository.findById(20L)).thenReturn(Optional.of(category1));
            when(categoryRepository.findById(30L)).thenReturn(Optional.of(category2));

            Ticket savedTicket = Ticket.builder()
                    .id(1L)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .deadline(request.getDeadline())
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

            // when
            Long ticketId = ticketService.createTicket(request, files, userDetails);

            // then
            assertThat(ticketId).isEqualTo(1L);
            verify(ticketRepository, times(1)).save(any(Ticket.class));
        }

//        @Test
//        @DisplayName("제목이 비어있을 때 오류가 발생한다. (컨트롤러, DB 단에서 체크)")
//        void should_ThrowException_When_TitleIsBlank() {
//            // given
//            CreateTicketRequest request = CreateTicketRequest.builder()
//                    .title(null)
//                    .description("티켓 설명")
//                    .typeId(10L)
//                    .firstCategoryId(20L)
//                    .secondCategoryId(30L)
//                    .deadline(LocalDateTime.now().plusDays(3))
//                    .urgent(false)
//                    .build();
//            List<MultipartFile> files = Collections.emptyList();
//
//            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
//            when(ticketTypeRepository.findById(10L)).thenReturn(Optional.of(ticketType));
//            when(categoryRepository.findById(20L)).thenReturn(Optional.of(category1));
//            when(categoryRepository.findById(30L)).thenReturn(Optional.of(category2));
//
//            // when & then
//            assertThatThrownBy(() -> ticketService.createTicket(request, files, userDetails))
//                    .isInstanceOf(IllegalArgumentException.class);
//        }

//        @Test
//        @DisplayName("내용이 비어있을 때 오류가 발생한다. (컨트롤러, DB 단에서 체크)")
//        void should_ThrowException_When_DescriptionIsBlank() {
//            // given
//            CreateTicketRequest request = CreateTicketRequest.builder()
//                    .title("테스트 티켓")
//                    .description(null)
//                    .typeId(10L)
//                    .firstCategoryId(20L)
//                    .secondCategoryId(30L)
//                    .deadline(LocalDateTime.now().plusDays(3))
//                    .urgent(false)
//                    .build();
//            List<MultipartFile> files = Collections.emptyList();
//
//            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
//            when(ticketTypeRepository.findById(10L)).thenReturn(Optional.of(ticketType));
//            when(categoryRepository.findById(20L)).thenReturn(Optional.of(category1));
//            when(categoryRepository.findById(30L)).thenReturn(Optional.of(category2));
//
//            // When & Then
//            assertThatThrownBy(() -> ticketService.createTicket(request, files, userDetails))
//                    .isInstanceOf(IllegalArgumentException.class);
//        }

        @Test
        @DisplayName("티켓 유형 비어있을 때 오류가 발생한다.")
        void should_ThrowException_When_TicketTypeIsBlank() {
            // given
            CreateTicketRequest request = new CreateTicketRequest(
                    "테스트 티켓",
                    "티켓 설명",
                    null,
                    20L,
                    30L,
                    LocalDateTime.now().plusDays(3),
                    null,
                    false
            );
            List<MultipartFile> files = Collections.emptyList();

            // when & then
            assertThatThrownBy(() -> ticketService.createTicket(request, files, userDetails))
                    .isInstanceOf(TicketTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("티켓 조회 테스트")
    class DescribeGetTicket {

        @Test
        @DisplayName("유효한 티켓 아이디로 조회하면 티켓 정보를 반환해야 한다.")
        void should_GetTicket_When_ValidRequest() {
            // given
            TicketResponse ticketResponse = new TicketResponse();
            when(ticketRepository.getTicket(eq(1L), anyLong(), anyString())).thenReturn(ticketResponse);
            List<AttachmentResponse> attachments = Collections.singletonList(new AttachmentResponse());
            when(attachmentRepository.getTicketAttachments(1L)).thenReturn(attachments);

            // when
            TicketResponse response = ticketService.getTicket(1L, userDetails);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAttachments()).isEqualTo(attachments);
        }

        @Test
        @DisplayName("아무 조건 없이 티켓 목록을 조회하면 전체 페이징 된 티켓 목록을 반환해야 한다.")
        void should_ReturnTicketList_when_GetTicketWithNoCondition() {
            // given
            List<TicketListResponse> list = Arrays.asList(
                    new TicketListResponse(
                            1L, "테스트 티켓", "티켓 설명", "생성", "카테고리1",
                            "카테고리2", "manager.ts", Ticket.Status.PENDING, false, Ticket.Priority.LOW,
                            LocalDateTime.now(), LocalDateTime.now()),
                    new TicketListResponse(
                            2L, "테스트 티켓1", "티켓 설명2", "생성", "카테고리1",
                            "카테고리2", "manager.ts", Ticket.Status.DONE, false, Ticket.Priority.HIGH,
                            LocalDateTime.now(), LocalDateTime.now())
            );
            Page<TicketListResponse> page = new PageImpl<>(list);

            when(userRepository.existsById(eq(userDetails.getId()))).thenReturn(true);
            when(ticketRepository.getTicketList(
                    any(Pageable.class), any(), any(), any(), any(), any(), any(), anyString(), anyString(), nullable(String.class))
            ).thenReturn(page);
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<TicketListResponse> result = ticketService.getTicketList(
                    pageable, Ticket.Status.PENDING, null, null, null,
                    null, userDetails.getId(), null, "newest", userDetails
            );

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("상태 필터가 적용되면 해당 상태의 티켓 목록만 반환되어야 한다.")
        void should_ReturnTicketListFilteredByStatus_when_StatusFilterApplied() {
            // given
            Ticket.Status filterStatus = Ticket.Status.IN_PROGRESS;
            List<TicketListResponse> list = Arrays.asList(
                    new TicketListResponse(
                            1L, "테스트 티켓", "티켓 설명", "생성", "카테고리1",
                            "카테고리2", "manager.ts", Ticket.Status.PENDING, false, Ticket.Priority.LOW,
                            LocalDateTime.now(), LocalDateTime.now()),
                    new TicketListResponse(
                            2L, "테스트 티켓1", "티켓 설명2", "생성", "카테고리1",
                            "카테고리2", "manager.ts", Ticket.Status.IN_PROGRESS, false, Ticket.Priority.HIGH,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            List<TicketListResponse> filteredList = list.stream()
                    .filter(ticket -> ticket.getStatus() == filterStatus)
                    .collect(Collectors.toList());

            Page<TicketListResponse> page = new PageImpl<>(filteredList);
            when(userRepository.existsById(eq(userDetails.getId()))).thenReturn(true);
            when(ticketRepository.getTicketList(
                    any(Pageable.class), eq(filterStatus), any(), any(), any(), any(), any(), anyString(), anyString(), nullable(String.class)
            )).thenReturn(page);
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<TicketListResponse> result = ticketService.getTicketList(
                    pageable, filterStatus, null, null, null,
                    null, userDetails.getId(), null, "newest", userDetails
            );

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(filterStatus);
        }

        @Test
        @DisplayName("1차/2차 카테고리 필터가 적용되면 해당 카테고리의 티켓 목록만 반환되어야 한다.")
        void should_ReturnTicketListFilteredByCategory_when_CategoryFilterApplied() {
            // given
            Long firstCategoryId = 20L;
            Long secondCategoryId = 30L;

            List<TicketListResponse> list = Arrays.asList(
                    new TicketListResponse(
                            4L, "카테고리 티켓1", "설명1", "생성", "카테고리1",
                            "카테고리2", "manager.ts", Ticket.Status.PENDING, false, Ticket.Priority.LOW,
                            LocalDateTime.now(), LocalDateTime.now()),
                    new TicketListResponse(
                            5L, "카테고리 티켓2", "설명2", "생성", "카테고리1",
                            "카테고리2", "manager.ts", Ticket.Status.IN_PROGRESS, false, Ticket.Priority.HIGH,
                            LocalDateTime.now(), LocalDateTime.now()),
                    new TicketListResponse(
                            6L, "비필터 티켓", "설명3", "생성", "다른카테고리",
                            "다른카테고리", "manager.ts", Ticket.Status.PENDING, false, Ticket.Priority.LOW,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            List<TicketListResponse> filteredList = list.stream()
                    .filter(ticket -> ticket.getFirstCategoryName().equalsIgnoreCase("카테고리1"))
                    .filter(ticket -> ticket.getSecondCategoryName().equalsIgnoreCase("카테고리2"))
                    .collect(Collectors.toList());
            Page<TicketListResponse> page = new PageImpl<>(filteredList);

            when(userRepository.existsById(eq(userDetails.getId()))).thenReturn(true);
            when(categoryRepository.findById(firstCategoryId)).thenReturn(Optional.of(category1));
            when(categoryRepository.findById(secondCategoryId)).thenReturn(Optional.of(category2));
            when(ticketRepository.getTicketList(
                    any(Pageable.class), any(), eq(firstCategoryId), eq(secondCategoryId), any(), any(), any(), anyString(), anyString(), nullable(String.class))
            ).thenReturn(page);

            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<TicketListResponse> result = ticketService.getTicketList(
                    pageable, null, firstCategoryId, secondCategoryId, null, null, userDetails.getId(), null, "newest", userDetails
            );

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            result.forEach(ticket -> {
                assertThat(ticket.getFirstCategoryName()).isEqualTo("카테고리1");
                assertThat(ticket.getSecondCategoryName()).isEqualTo("카테고리2");
            });
        }

        @Test
        @DisplayName("티켓 유형 필터가 적용되면 해당 유형의 티켓 목록만 반환되어야 한다.")
        void should_ReturnTicketListFilteredByTicketType_when_TicketTypeFilterApplied() {
            // given
            Long ticketTypeId = 10L;

            List<TicketListResponse> list = Arrays.asList(
                    new TicketListResponse(
                            1L, "유형 티켓1", "설명1", "수정", "카테고리1",
                            "카테고리2", "manager.ts", Ticket.Status.PENDING, false, Ticket.Priority.LOW,
                            LocalDateTime.now(), LocalDateTime.now()),
                    new TicketListResponse(
                            3L, "비필터 티켓", "설명3", "생성", "다른카테고리",
                            "다른카테고리", "manager.ts", Ticket.Status.PENDING, false, Ticket.Priority.LOW,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            List<TicketListResponse> filteredList = list.stream()
                    .filter(ticket -> ticket.getTypeName().equalsIgnoreCase("생성"))
                    .collect(Collectors.toList());
            Page<TicketListResponse> page = new PageImpl<>(filteredList);

            when(userRepository.existsById(eq(userDetails.getId()))).thenReturn(true);
            when(ticketTypeRepository.existsById(ticketTypeId)).thenReturn(true);
            when(ticketRepository.getTicketList(any(Pageable.class), any(), any(), any(), eq(ticketTypeId), any(), any(), anyString(), anyString(), nullable(String.class)
            )).thenReturn(page);

            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<TicketListResponse> result = ticketService.getTicketList(
                    pageable, null, null, null, ticketTypeId, null, userDetails.getId(), null, "newest", userDetails
            );

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            result.forEach(ticket -> {
                assertThat(ticket.getTypeName()).isEqualTo("생성");
            });
        }

        @Test
        @DisplayName("담당자 필터가 적용되면 해당 담당자의 티켓 목록만 반환되어야 한다.")
        void should_ReturnTicketListFilteredByManager_when_ManagerFilterApplied() {
            // given
            Long managerId = 2L;
            User manager = User.builder()
                    .username("manager.ts")
                    .email("manager@email.com")
                    .role(Role.MANAGER)
                    .build();
            ReflectionTestUtils.setField(manager, "id", managerId);

            List<TicketListResponse> list = Arrays.asList(
                    new TicketListResponse(
                            1L, "담당자 티켓1", "설명1", "수정", "카테고리1",
                            "카테고리2", "manager.ts", Ticket.Status.PENDING, false, Ticket.Priority.LOW,
                            LocalDateTime.now(), LocalDateTime.now()),
                    new TicketListResponse(
                            2L, "담당자 티켓2", "설명2", "수정", "카테고리1",
                            "카테고리2", "managerDiff.ts", Ticket.Status.PENDING, false, Ticket.Priority.LOW,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            List<TicketListResponse> filteredList = list.stream()
                    .filter(ticket -> ticket.getManagerName().equalsIgnoreCase("manager.ts"))
                    .collect(Collectors.toList());
            Page<TicketListResponse> page = new PageImpl<>(filteredList);

            when(userRepository.existsById(eq(userDetails.getId()))).thenReturn(true);
            when(userRepository.existsById(eq(managerId))).thenReturn(true);
            when(ticketRepository.getTicketList(
                    any(Pageable.class), any(), any(), any(), any(), eq(managerId), any(), anyString(), anyString(), nullable(String.class)
            )).thenReturn(page);

            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<TicketListResponse> result = ticketService.getTicketList(
                    pageable, null, null, null, null, managerId, 1L, null, "newest", userDetails
            );

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            result.forEach(ticket -> {
                assertThat(ticket.getManagerName()).isEqualTo("manager.ts");
            });
        }

    }

    @Nested
    @DisplayName("티켓 수정 테스트 (사용자)")
    class DescribeUpdateTicketByUser {

        @Test
        @DisplayName("유효한 티켓 수정 요청이 들어오면 티켓의 제목, 내용, 마감일, 티켓 유형, 1차/2차 카테고리가 수정된다.")
        void should_UpdateTicketContent_when_ValidEditRequest() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("원래 제목")
                    .description("원래 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            ReflectionTestUtils.setField(ticket, "id", 1L);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            TicketType newTicketType = TicketType.builder().name("수정").build();
            ReflectionTestUtils.setField(newTicketType, "id", 11L);
            when(ticketTypeRepository.findById(11L)).thenReturn(Optional.of(newTicketType));

            Category newCategory1 = Category.builder().name("수정 카테고리1").build();
            ReflectionTestUtils.setField(newCategory1, "id", 21L);
            Category newCategory2 = Category.builder().name("수정 카테고리2").parent(newCategory1).build();
            ReflectionTestUtils.setField(newCategory2, "id", 31L);
            when(categoryRepository.findById(21L)).thenReturn(Optional.of(newCategory1));
            when(categoryRepository.findById(31L)).thenReturn(Optional.of(newCategory2));

            EditTicketRequest editRequest = EditTicketRequest.builder()
                    .title("수정된 제목")
                    .description("수정된 설명")
                    .deadline(LocalDateTime.now().plusDays(2))
                    .ticketTypeId(11L)
                    .firstCategoryId(21L)
                    .secondCategoryId(31L)
                    .build();

            // when
            ticketService.editTicket(editRequest, 1L, userDetails);

            // then
            assertThat(ticket.getTitle()).isEqualTo("수정된 제목");
            assertThat(ticket.getDescription()).isEqualTo("수정된 설명");
            assertThat(ticket.getDeadline()).isEqualTo(editRequest.getDeadline());
            assertThat(ticket.getTicketType()).isEqualTo(newTicketType);
            assertThat(ticket.getFirstCategory()).isEqualTo(newCategory1);
            assertThat(ticket.getSecondCategory()).isEqualTo(newCategory2);
        }

        @Test
        @DisplayName("존재하지 않는 티켓을 수정 요청하면 오류가 발생한다.")
        void should_ThrowTicketNotFoundException_when_TicketDoesNotExist() {
            // given
            when(ticketRepository.findById(1L)).thenReturn(Optional.empty());
            EditTicketRequest editRequest = EditTicketRequest.builder().title("수정된 제목").build();

            // when & then
            assertThatThrownBy(() -> ticketService.editTicket(editRequest, 1L, userDetails))
                    .isInstanceOf(TicketNotFoundException.class);
        }

        @Test
        @DisplayName("새로운 티켓 유형이 존재하지 않으면 오류가 발생한다.")
        void should_ThrowTicketTypeNotFoundException_when_NewTicketTypeNotExist() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("원래 제목")
                    .description("원래 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            ReflectionTestUtils.setField(ticket, "id", 1L);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            EditTicketRequest editRequest = EditTicketRequest.builder()
                    .ticketTypeId(999L)
                    .build();
            when(ticketTypeRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketService.editTicket(editRequest, 1L, userDetails))
                    .isInstanceOf(TicketTypeNotFoundException.class);
        }

        @Test
        @DisplayName("새로운 카테고리가 존재하지 않으면 오류가 발생한다.")
        void should_ThrowCategoryNotFoundException_when_NewCategoryNotExist() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("원래 제목")
                    .description("원래 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            ReflectionTestUtils.setField(ticket, "id", 1L);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            EditTicketRequest editRequest = EditTicketRequest.builder().firstCategoryId(21L).build();
            when(categoryRepository.findById(21L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketService.editTicket(editRequest, 1L, userDetails))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("카테고리 관계가 올바르지 않으면 오류가 발생한다.")
        void should_ThrowInvalidCategoryLevelException_when_CategoryRelationInvalid() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("원래 제목")
                    .description("원래 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            ReflectionTestUtils.setField(ticket, "id", 1L);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            EditTicketRequest editRequest = EditTicketRequest.builder()
                    .firstCategoryId(21L)
                    .secondCategoryId(31L)
                    .build();

            Category newCategory1 = Category.builder().name("카테고리99").build();
            ReflectionTestUtils.setField(newCategory1, "id", 21L);
            Category newCategory2 = Category.builder().name("카테고리1").parent(mock(Category.class)).build();
            ReflectionTestUtils.setField(newCategory2, "id", 31L);

            when(categoryRepository.findById(21L)).thenReturn(Optional.of(newCategory1));
            when(categoryRepository.findById(31L)).thenReturn(Optional.of(newCategory2));

            // when & then
            assertThatThrownBy(() -> ticketService.editTicket(editRequest, 1L, userDetails))
                    .isInstanceOf(InvalidCategoryLevelException.class);
        }
    }

    @Nested
    @DisplayName("티켓 수정 테스트 (담당자)")
    class DescribeUpdateTicketByManager {

        @Test
        @DisplayName("담당자가 티켓 유형을 수정하면 티켓의 유형이 변경되어야 한다.")
        void should_UpdateTicketType_when_ManagerEditsTicketType() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("원래 제목")
                    .description("원래 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            ReflectionTestUtils.setField(ticket, "id", 1L);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            TicketType newTicketType = TicketType.builder().name("수정 타입").build();
            ReflectionTestUtils.setField(newTicketType, "id", 11L);
            when(ticketTypeRepository.findById(11L)).thenReturn(Optional.of(newTicketType));

            // when
            ticketService.editTypeForManager(1L, 11L, userDetails);

            // then
            assertThat(ticket.getTicketType()).isEqualTo(newTicketType);
        }

        @Test
        @DisplayName("담당자가 티켓 담당자를 수정하면 티켓의 담당자가 변경되어야 한다.")
        void should_UpdateTicketManager_when_ManagerEditsTicketManager() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("원래 제목")
                    .description("원래 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            ReflectionTestUtils.setField(ticket, "id", 1L);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            User newManager = User.builder().username("newManager").email("manager@new.com").role(Role.MANAGER).build();
            ReflectionTestUtils.setField(newManager, "id", 2L);
            when(userRepository.findById(2L)).thenReturn(Optional.of(newManager));

            // when
            ticketService.editManager(1L, 2L, userDetails);

            // then
            assertThat(ticket.getManager()).isEqualTo(newManager);
        }

        @Test
        @DisplayName("담당자가 티켓 카테고리를 수정하면 티켓의 카테고리가 변경되어야 한다.")
        void should_UpdateTicketCategory_when_ManagerEditsTicketCategory() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("원래 제목")
                    .description("원래 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .firstCategory(category1)
                    .secondCategory(category2)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            ReflectionTestUtils.setField(ticket, "id", 1L);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            Category newCategory1 = Category.builder().name("수정 카테고리1").build();
            ReflectionTestUtils.setField(newCategory1, "id", 21L);
            Category newCategory2 = Category.builder().name("수정 카테고리2").parent(newCategory1).build();
            ReflectionTestUtils.setField(newCategory2, "id", 31L);
            when(categoryRepository.findById(21L)).thenReturn(Optional.of(newCategory1));
            when(categoryRepository.findById(31L)).thenReturn(Optional.of(newCategory2));

            EditCategory editCategory = new EditCategory();
            editCategory.setFirstCategoryId(21L);
            editCategory.setSecondCategoryId(31L);

            // when
            ticketService.editCategoryForManager(editCategory, 1L, userDetails);

            // then
            assertThat(ticket.getFirstCategory()).isEqualTo(newCategory1);
            assertThat(ticket.getSecondCategory()).isEqualTo(newCategory2);
        }

        @Test
        @DisplayName("담당자가 티켓 마감기한을 수정하면 티켓의 마감기한이 변경되어야 한다.")
        void should_UpdateTicketDeadline_when_ManagerEditsDeadline() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("원래 제목")
                    .description("원래 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            ReflectionTestUtils.setField(ticket, "id", 1L);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            LocalDateTime newDeadline = LocalDateTime.now().plusDays(5);
            EditSettingRequest settingRequest = EditSettingRequest.builder()
                    .deadline(newDeadline)
                    .build();

            // when
            ticketService.editDeadlineForManager(1L, settingRequest, userDetails);

            // then
            assertThat(ticket.getDeadline()).isEqualTo(newDeadline);
        }

        @Test
        @DisplayName("담당자가 티켓 우선순위를 수정하면 티켓의 우선순위가 변경되어야 한다.")
        void should_UpdateTicketPriority_when_ManagerEditsPriority() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("원래 제목")
                    .description("원래 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            ReflectionTestUtils.setField(ticket, "id", 1L);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // when
            ticketService.editPriority(1L, Ticket.Priority.HIGH, userDetails);

            // then
            assertThat(ticket.getPriority()).isEqualTo(Ticket.Priority.HIGH);
        }

        @Test
        @DisplayName("담당자가 티켓 상태를 수정하면 티켓의 상태가 변경되어야 한다.")
        void should_UpdateTicketStatus_when_ManagerEditsStatus() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("원래 제목")
                    .description("원래 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            ReflectionTestUtils.setField(ticket, "id", 1L);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // when
            ticketService.editStatus(1L, Ticket.Status.IN_PROGRESS, userDetails);

            // then
            assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.IN_PROGRESS);
        }
    }

    @Nested
    @DisplayName("티켓 삭제 테스트")
    class DescribeDeleteTicket {

        @Test
        @DisplayName("요청자가 본인의 PENDING 티켓을 삭제하면 티켓이 삭제되어야 한다.")
        void should_DeleteTicket_when_RequesterDeletesOwnPendingTicket() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("테스트 티켓")
                    .description("티켓 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            // when
            ticketService.deleteTicket(100L, userDetails);

            // then
            verify(ticketRepository, times(1)).delete(ticket);
        }

        @Test
        @DisplayName("존재하지 않는 티켓을 삭제 요청하면 오류가 발생한다.")
        void should_ThrowTicketNotFoundException_when_TicketDoesNotExist() {
            // given
            when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketService.deleteTicket(1L, userDetails))
                    .isInstanceOf(TicketNotFoundException.class);
        }

        @Test
        @DisplayName("티켓 요청자 정보가 조회되지 않으면 오류가 발생한다.")
        void should_ThrowUserNotFoundException_when_RequesterNotFound() {
            // given
            Ticket ticket = Ticket.builder()
                    .title("테스트 티켓")
                    .description("티켓 설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(ticketType)
                    .requester(user)
                    .status(Ticket.Status.PENDING)
                    .build();
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketService.deleteTicket(100L, userDetails))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("티켓 상태가 대기가 아닐 때 삭제 요청 시 오류가 발생한다.")
        void should_ThrowUnauthorizedTicketAccessException_when_TicketStatusNotPending() {
            // given
            Ticket ticketNotPending = Ticket.builder()
                    .title("티켓")
                    .description("설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(null)
                    .requester(user)
                    .status(Ticket.Status.IN_PROGRESS)
                    .build();
            ReflectionTestUtils.setField(ticketNotPending, "id", 101L);
            when(ticketRepository.findById(101L)).thenReturn(Optional.of(ticketNotPending));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> ticketService.deleteTicket(101L, userDetails))
                    .isInstanceOf(UnauthorizedTicketAccessException.class);
        }

        @Test
        @DisplayName("요청자와 티켓 요청자가 다를 때 삭제 요청 시 오류가 발생한다.")
        void should_ThrowUnauthorizedTicketAccessException_when_RequesterMismatch() {
            // given
            User anotherUser = User.builder()
                    .username("anotherUser")
                    .email("another@example.com")
                    .role(Role.USER)
                    .build();
            ReflectionTestUtils.setField(anotherUser, "id", 2L);
            Ticket ticketDifferentRequester = Ticket.builder()
                    .title("티켓")
                    .description("설명")
                    .deadline(LocalDateTime.now().plusDays(1))
                    .ticketType(null)
                    .requester(anotherUser)
                    .status(Ticket.Status.PENDING)
                    .build();
            ReflectionTestUtils.setField(ticketDifferentRequester, "id", 102L);
            when(ticketRepository.findById(102L)).thenReturn(Optional.of(ticketDifferentRequester));
            when(userRepository.findById(anotherUser.getId())).thenReturn(Optional.of(anotherUser));

            // when & then
            assertThatThrownBy(() -> ticketService.deleteTicket(102L, userDetails))
                    .isInstanceOf(UnauthorizedTicketAccessException.class);
        }
    }
}
