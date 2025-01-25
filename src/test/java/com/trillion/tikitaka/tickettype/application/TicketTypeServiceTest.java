package com.trillion.tikitaka.tickettype.application;

import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.dto.response.TicketTypeListResponse;
import com.trillion.tikitaka.tickettype.exception.DuplicatedTicketTypeException;
import com.trillion.tikitaka.tickettype.exception.TicketTypeNotFoundException;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("티켓 유형 유닛 테스트")
class TicketTypeServiceTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    private TicketTypeService ticketTypeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ticketTypeService = new TicketTypeService(ticketTypeRepository);
    }

    @Nested
    @DisplayName("티켓 유형 생성 테스트")
    class DescribeCreateTicketType {

        @Test
        @DisplayName("새로운 티켓 타입 이름을 등록하면 저장에 성공한다.")
        void should_CreateNewTicketType_when_TypeNameValid() {
            // given
            String typeName = "생성";
            when(ticketTypeRepository.findByName(typeName))
                    .thenReturn(Optional.empty());

            // when
            ticketTypeService.createTicketType(typeName);

            // then
            verify(ticketTypeRepository, times(1)).save(any(TicketType.class));
        }

        @Test
        @DisplayName("이미 존재하는 티켓 타입 이름을 등록하면 예외가 발생한다.")
        void should_ThrowException_when_TypeNameDuplicated() {
            // given
            String duplicatedName = "생성";
            TicketType existingTicketType = TicketType.builder()
                    .name(duplicatedName)
                    .build();

            when(ticketTypeRepository.findByName(duplicatedName))
                    .thenReturn(Optional.of(existingTicketType));

            // when && then
            assertThatThrownBy(() -> ticketTypeService.createTicketType(duplicatedName))
                    .isInstanceOf(DuplicatedTicketTypeException.class);
        }
    }

    @Nested
    @DisplayName("티켓 유형 조회 테스트")
    class DescribeGetTicketTypes {

        @Test
        @DisplayName("존재하는 티켓 유형 목록을 정상적으로 조회한다")
        void should_ReturnTicketTypeList_when_TicketTypesExist() {
            // given
            List<TicketTypeListResponse> mockList = List.of(
                    new TicketTypeListResponse(1L, "생성"),
                    new TicketTypeListResponse(2L, "변경")
            );

            when(ticketTypeRepository.getTicketTypes())
                    .thenReturn((mockList));

            // when
            List<TicketTypeListResponse> ticketTypes = ticketTypeService.getTicketTypes();

            // then
            assertThat(ticketTypes).hasSize(2);
            assertThat(ticketTypes.get(0).getTypeName()).isEqualTo("생성");
            assertThat(ticketTypes.get(1).getTypeName()).isEqualTo("변경");
        }

        @Test
        @DisplayName("존재하지 않는 티켓 유형 목록을 조회하면 빈 목록을 반환한다")
        void should_ReturnEmptyList_when_TicketTypesNotExist() {
            // given
            when(ticketTypeRepository.getTicketTypes())
                    .thenReturn(List.of());

            // when
            List<TicketTypeListResponse> ticketTypes = ticketTypeService.getTicketTypes();

            // then
            assertThat(ticketTypes).isEmpty();
        }
    }

    @Nested
    @DisplayName("티켓 유형 수정 테스트")
    class DescribeUpdateTicketType {

        @Test
        @DisplayName("유효한 티켓 유형 ID와 티켓 유형명으로 이름을 수정하면 성공한다.")
        void should_UpdateTicketTypeName_when_TypeIdAndTypeNameValid() {
            // given
            Long typeId = 1L;
            String typeName = "생성";
            TicketType existingTicketType = TicketType.builder()
                    .name("변경")
                    .build();
            ReflectionTestUtils.setField(existingTicketType, "id", 1L);

            when(ticketTypeRepository.findByIdAndNameCheck(typeId, typeName))
                    .thenReturn(List.of(existingTicketType));

            // when
            ticketTypeService.updateTicketType(typeId, typeName);

            // given
            assertThat(existingTicketType.getName()).isEqualTo(typeName);
        }

        @Test
        @DisplayName("주어진 ID의 티켓 유형이 존재하지 않으면 예외가 발생한다.")
        void should_ThrowException_when_TicketTypeNotFound() {
            // given
            Long typeId = 1L;
            String typeName = "생성";

            when(ticketTypeRepository.findByIdAndNameCheck(typeId, typeName))
                    .thenReturn(List.of());

            // when && then
            assertThatThrownBy(() -> ticketTypeService.updateTicketType(typeId, typeName))
                    .isInstanceOf(TicketTypeNotFoundException.class);
        }

        @Test
        @DisplayName("이미 존재하는 티켓 유형명으로 수정하면 예외가 발생한다.")
        void should_ThrowException_when_TypeNameDuplicated() {
            // given
            Long typeId = 1L;
            String typeName = "생성";
            TicketType existingTicketType1 = TicketType.builder()
                    .name("변경")
                    .build();
            ReflectionTestUtils.setField(existingTicketType1, "id", 1L);

            TicketType existingTicketType2 = TicketType.builder()
                    .name("생성")
                    .build();
            ReflectionTestUtils.setField(existingTicketType2, "id", 2L);

            when(ticketTypeRepository.findByIdAndNameCheck(typeId, typeName))
                    .thenReturn(List.of(existingTicketType1, existingTicketType2));

            // when && then
            assertThatThrownBy(() -> ticketTypeService.updateTicketType(typeId, typeName))
                    .isInstanceOf(DuplicatedTicketTypeException.class);
        }
    }

    @Nested
    @DisplayName("티켓 유형 삭제 테스트")
    class DescribeDeleteTicketType {

        @Test
        @DisplayName("존재하는 티켓 타입을 삭제하면 소프트 삭제가 수행된다.")
        void should_SoftDeleteTicketType_when_ExistingTicketTypeIsDeleted() {
            // given
            Long existingId = 1L;
            TicketType ticketType = TicketType.builder()
                    .name("생성")
                    .build();

            when(ticketTypeRepository.findById(existingId))
                    .thenReturn(Optional.of(ticketType));

            // when
            ticketTypeService.deleteTicketType(existingId);

            // then
            verify(ticketTypeRepository, times(1)).delete(ticketType);
        }

        @Test
        @DisplayName("존재하지 않는 티켓 타입을 삭제하려 하면 예외가 발생한다.")
        void should_ThrowTicketTypeNotFoundException_when_DeleteNonExistingTicketType() {
            // given
            Long nonExistingId = 999L;
            when(ticketTypeRepository.findById(nonExistingId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketTypeService.deleteTicketType(nonExistingId))
                    .isInstanceOf(TicketTypeNotFoundException.class);
        }
    }
}