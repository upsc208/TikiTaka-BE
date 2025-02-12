package com.trillion.tikitaka.ticketform;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticketform.application.TicketFormService;
import com.trillion.tikitaka.ticketform.domain.TicketForm;
import com.trillion.tikitaka.ticketform.domain.TicketFormId;
import com.trillion.tikitaka.ticketform.exception.TicketFormNotFoundException;
import com.trillion.tikitaka.ticketform.exception.DuplicatedTicketFormException;
import com.trillion.tikitaka.ticketform.infrastructure.TicketFormRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("티켓 폼 유닛 테스트")
public class TicketFormServiceTest {

    @Mock
    private TicketFormRepository ticketFormRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private TicketFormService ticketFormService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ticketFormService = new TicketFormService(ticketFormRepository, categoryRepository);
    }

    @Nested
    @DisplayName("티켓 폼 생성 테스트")
    class DescribeCreateTicketForm {

        @Test
        @DisplayName("1, 2차 카테고리가 존재하고 유효한 경우 티켓 폼을 생성한다.")
        void should_CreateTicketForm_when_ValidRequest() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;
            String description = "Ticket Form Test";
            String mustDescription = "필수 설명";

            Category firstCategory = mock(Category.class);
            Category secondCategory = mock(Category.class);

            when(categoryRepository.findById(firstCategoryId))
                    .thenReturn(Optional.of(firstCategory));
            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.isChildOf(firstCategory)).thenReturn(true);

            TicketFormId formId = new TicketFormId(firstCategoryId, secondCategoryId);
            when(ticketFormRepository.existsById(formId)).thenReturn(false);

            // when
            ticketFormService.createTicketForm(firstCategoryId, secondCategoryId, description, mustDescription);

            // then
            verify(ticketFormRepository, times(1)).save(any(TicketForm.class));
        }

        @Test
        @DisplayName("이미 존재하는 1,2차 카테고리 ID로 생성하면 예외가 발생한다.")
        void should_ThrowException_When_DuplicatedTicketFormExists() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;
            String description = "Duplicate Ticket Form";
            String mustDescription = "필수 설명";

            Category firstCategory = mock(Category.class);
            Category secondCategory = mock(Category.class);

            when(categoryRepository.findById(firstCategoryId))
                    .thenReturn(Optional.of(firstCategory));
            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.isChildOf(firstCategory)).thenReturn(true);

            TicketFormId formId = new TicketFormId(firstCategoryId, secondCategoryId);
            when(ticketFormRepository.existsById(formId)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> ticketFormService.createTicketForm(firstCategoryId, secondCategoryId, description, mustDescription))
                    .isInstanceOf(DuplicatedTicketFormException.class)
                    .hasMessage("이미 존재하는 티켓 폼입니다.");

            verify(ticketFormRepository, never()).save(any(TicketForm.class));
        }


        @Test
        @DisplayName("존재하지 않는 1차 카테고리 ID로 생성하면 예외가 발생한다.")
        void should_ThrowException_when_FirstCategoryNotFound() {

            Long invalidFirstCategoryId = 999L;
            Long secondCategoryId = 2L;

            when(categoryRepository.findById(invalidFirstCategoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketFormService.createTicketForm(invalidFirstCategoryId, secondCategoryId, "설명", "필수 설명"))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("티켓 폼 조회 테스트")
    class DescribeGetTicketForm {

        @Test
        @DisplayName("정상적으로 티켓 폼이 존재하면 내용을 담은 응답을 반환한다.")
        void should_ReturnTicketForm_when_TicketFormExists() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;
            String description = "Ticket Form Tests";
            String mustDescription = "Ticket Form Test";

            Category firstCategory = mock(Category.class);
            Category secondCategory = mock(Category.class);

            lenient().when(firstCategory.getId()).thenReturn(firstCategoryId);
            lenient().when(secondCategory.getId()).thenReturn(secondCategoryId);
            lenient().when(secondCategory.getParent()).thenReturn(firstCategory);

            lenient().when(categoryRepository.findById(firstCategoryId)).thenReturn(Optional.of(firstCategory));
            lenient().when(categoryRepository.findById(secondCategoryId)).thenReturn(Optional.of(secondCategory));

            TicketForm ticketForm = mock(TicketForm.class);
            lenient().when(ticketForm.getDescription()).thenReturn(description);
            lenient().when(ticketForm.getMustDescription()).thenReturn(mustDescription);

            when(ticketFormRepository.findById(any(TicketFormId.class))).thenReturn(Optional.of(ticketForm));

            // when
            ticketFormService.getTicketForm(firstCategoryId, secondCategoryId);

            // then
            assertThat(ticketForm.getDescription()).isEqualTo(description);
            assertThat(ticketForm.getMustDescription()).isEqualTo(mustDescription);
        }


        @Test
        @DisplayName("존재하지 않는 티켓 폼을 조회하면 null값을 반환한다.")
        void should_ThrowException_when_TicketFormNotFound() {
            // given
            Long firstCategoryId = 999L;
            Long secondCategoryId = 2L;

            Category firstCategory = mock(Category.class);
            Category secondCategory = mock(Category.class);

            when(firstCategory.getId()).thenReturn(firstCategoryId);
            when(secondCategory.getId()).thenReturn(secondCategoryId);
            when(secondCategory.getParent()).thenReturn(firstCategory);

            when(categoryRepository.findById(firstCategoryId)).thenReturn(Optional.of(firstCategory));
            when(categoryRepository.findById(secondCategoryId)).thenReturn(Optional.of(secondCategory));

            TicketFormId ticketFormId = new TicketFormId(firstCategoryId, secondCategoryId);
            when(ticketFormRepository.findById(eq(ticketFormId))).thenReturn(null);

            assertThat(ticketFormRepository.findById(ticketFormId)).isEqualTo(null);
            verify(ticketFormRepository, times(1)).findById(eq(ticketFormId));
        }
    }


    @Nested
    @DisplayName("티켓 폼 수정 테스트")
    class DescribeUpdateTicketForm {

        @Test
        @DisplayName("존재하는 티켓 폼의 설명을 변경하면 정상적으로 변경된다.")
        void should_UpdateTicketFormDescription_when_FormExists() {

            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;
            String description = "새로운 설명";
            String mustDescription = "새로운 필수 설명";

            Category secondCategory = mock(Category.class);
            TicketForm ticketForm = mock(TicketForm.class);

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.getParent()).thenReturn(mock(Category.class));
            when(secondCategory.getParent().getId()).thenReturn(firstCategoryId);

            TicketFormId formId = new TicketFormId(firstCategoryId, secondCategoryId);
            when(ticketFormRepository.findById(formId))
                    .thenReturn(Optional.of(ticketForm));

            ticketFormService.updateTicketForm(firstCategoryId, secondCategoryId, description, mustDescription);

            verify(ticketForm, times(1)).update(description, mustDescription);
        }

        @Test
        @DisplayName("존재하지 않는 티켓 폼을 수정하려 하면 예외가 발생한다.")
        void should_ThrowException_when_UpdatingNonExistingTicketForm() {
            // given
            Long firstCategoryId = 999L;
            Long secondCategoryId = 2L;
            String description = "변경된 제목";
            String mustDescription = "변경된 내용";

            Category firstCategory = mock(Category.class);
            Category secondCategory = mock(Category.class);

            when(firstCategory.getId()).thenReturn(firstCategoryId);
            when(secondCategory.getId()).thenReturn(secondCategoryId);
            when(secondCategory.getParent()).thenReturn(firstCategory);

            when(categoryRepository.findById(firstCategoryId)).thenReturn(Optional.of(firstCategory));
            when(categoryRepository.findById(secondCategoryId)).thenReturn(Optional.of(secondCategory));

            when(ticketFormRepository.findById(any(TicketFormId.class))).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketFormService.updateTicketForm(firstCategoryId, secondCategoryId, description, mustDescription))
                    .isInstanceOf(TicketFormNotFoundException.class);

            verify(ticketFormRepository, times(1)).findById(any(TicketFormId.class));
        }

    }

    @Nested
    @DisplayName("티켓 폼 삭제 테스트")
    class DescribeDeleteTicketForm {

        @Test
        @DisplayName("티켓 폼이 존재하면 정상적으로 삭제된다.")
        void should_DeleteTicketForm_when_FormExists() {

            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;

            Category secondCategory = mock(Category.class);
            TicketForm ticketForm = mock(TicketForm.class);

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.getParent()).thenReturn(mock(Category.class));
            when(secondCategory.getParent().getId()).thenReturn(firstCategoryId);

            TicketFormId formId = new TicketFormId(firstCategoryId, secondCategoryId);
            when(ticketFormRepository.findById(formId))
                    .thenReturn(Optional.of(ticketForm));

            ticketFormService.deleteTicketForm(firstCategoryId, secondCategoryId);

            verify(ticketFormRepository, times(1)).delete(ticketForm);
        }

        @Test
        @DisplayName("존재하지 않는 티켓 폼을 삭제하면 예외가 발생한다.")
        void should_ThrowException_when_DeletingNonExistingTicketForm() {

            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;

            Category firstCategory = mock(Category.class);
            Category secondCategory = mock(Category.class);

            when(firstCategory.getId()).thenReturn(firstCategoryId);
            when(secondCategory.getId()).thenReturn(secondCategoryId);
            when(secondCategory.getParent()).thenReturn(firstCategory);

            when(categoryRepository.findById(firstCategoryId)).thenReturn(Optional.of(firstCategory));
            when(categoryRepository.findById(secondCategoryId)).thenReturn(Optional.of(secondCategory));

            when(ticketFormRepository.findById(any(TicketFormId.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketFormService.deleteTicketForm(firstCategoryId, secondCategoryId))
                    .isInstanceOf(TicketFormNotFoundException.class);
        }


    }
}