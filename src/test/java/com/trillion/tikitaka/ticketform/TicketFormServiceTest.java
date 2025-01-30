package com.trillion.tikitaka.ticketform;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.InvalidCategoryLevelException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticketform.application.TicketFormService;
import com.trillion.tikitaka.ticketform.domain.TicketForm;
import com.trillion.tikitaka.ticketform.domain.TicketFormId;
import com.trillion.tikitaka.ticketform.dto.response.TicketFormResponse;
import com.trillion.tikitaka.ticketform.exception.DuplicatedTicketFormException;
import com.trillion.tikitaka.ticketform.exception.TicketFormNotFoundException;
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
import static org.mockito.Mockito.when;

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
            ticketFormService.createTicketForm(firstCategoryId, secondCategoryId, description);

            // then
            verify(ticketFormRepository, times(1)).save(any(TicketForm.class));
        }

        @Test
        @DisplayName("1차 카테고리가 존재하지 않으면 오류가 발생한다.")
        void should_ThrowException_when_FirstCategoryDoesNotExist() {
            // given
            Long invalidFirstCategoryId = 999L;
            Long secondCategoryId = 2L;
            String description = "Ticket Form Test";

            when(categoryRepository.findById(invalidFirstCategoryId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketFormService.createTicketForm((invalidFirstCategoryId), secondCategoryId, description))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("2차 카테고리가 존재하지 않으면 오류가 발생한다.")
        void should_ThrowException_when_secondCategoryDoesNotExist() {
            // given
            Long firstCategoryId = 1L;
            Long invalidSecondCategoryId = 999L;
            String description = "Ticket Form Test";

            Category firstCategory = mock(Category.class);

            when(categoryRepository.findById(firstCategoryId))
                    .thenReturn(Optional.of(firstCategory));
            when(categoryRepository.findById(invalidSecondCategoryId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketFormService.createTicketForm(firstCategoryId, invalidSecondCategoryId, description))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("2차 카테고리가 1차 카테고리 하위 카테고리가 아니면 오류가 발생한다.")
        void should_ThrowException_when_SecondCategoryIsNotChildOfFirstCategory() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;
            String description = "Ticket Form Test";

            Category firstCategory = mock(Category.class);
            Category secondCategory = mock(Category.class);

            when(categoryRepository.findById(firstCategoryId))
                    .thenReturn(Optional.of(firstCategory));
            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));

            when(secondCategory.isChildOf(firstCategory)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> ticketFormService.createTicketForm(firstCategoryId, secondCategoryId, description))
                    .isInstanceOf(InvalidCategoryLevelException.class);
        }

        @Test
        @DisplayName("입력한 카테고리에 대한 티켓 폼이 이미 존재한다면 오류가 발생한다.")
        void should_ThrowException_when_TicketFormAlreadyExists() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;
            String description = "Ticket Form Test";

            Category firstCategory = mock(Category.class);
            Category secondCategory = mock(Category.class);

            when(categoryRepository.findById(firstCategoryId))
                    .thenReturn(Optional.of(firstCategory));
            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));

            when(secondCategory.isChildOf(firstCategory)).thenReturn(true);

            TicketFormId formId = new TicketFormId(firstCategoryId, secondCategoryId);
            when(ticketFormRepository.existsById(formId))
                    .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> ticketFormService.createTicketForm(firstCategoryId, secondCategoryId, description))
                    .isInstanceOf(DuplicatedTicketFormException.class);
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
            String description = "Ticket Form Test";

            Category secondCategory = mock(Category.class);
            TicketForm ticketForm = mock(TicketForm.class);

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.getParent()).thenReturn(mock(Category.class));
            when(secondCategory.getParent().getId()).thenReturn(firstCategoryId);

            TicketFormId formId = new TicketFormId(firstCategoryId, secondCategoryId);
            when(ticketFormRepository.findById(formId))
                    .thenReturn(Optional.of(ticketForm));
            when(ticketForm.getDescription()).thenReturn(description);

            // when
            TicketFormResponse response = new TicketFormResponse(description);

            // then
            assertThat(response.getDescription()).isEqualTo(description);
        }

        @Test
        @DisplayName("2차 카테고리가 존재하지 않으면 오류가 발생한다.")
        void should_ThrowException_when_SecondCategoryDoesNotExist() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 999L;

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketFormService.getTicketForm(firstCategoryId, secondCategoryId))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("2차 카테고리가 1차 카테고리 하위 카테고리가 아니라면 오류가 발생한다.")
        void should_ThrowException_when_secondCategoryIsNotChildOfFirstCategory() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;
            Category secondCategory = mock(Category.class);

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.getParent()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> ticketFormService.getTicketForm(firstCategoryId, secondCategoryId))
                    .isInstanceOf(InvalidCategoryLevelException.class);
        }

        @Test
        @DisplayName("티켓 폼이 존재하지 않으면 빈 문자열을 반환한다.")
        void should_ReturnEmptyStringDescription_when_FormDoesNotExist() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;
            Category secondCategory = mock(Category.class);

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.getParent()).thenReturn(mock(Category.class));
            when(secondCategory.getParent().getId()).thenReturn(firstCategoryId);

            TicketFormId formId = new TicketFormId(firstCategoryId, secondCategoryId);
            when(ticketFormRepository.findById(formId))
                    .thenReturn(Optional.empty());

            // when
            TicketFormResponse response = ticketFormService.getTicketForm(firstCategoryId, secondCategoryId);

            // then
            assertThat(response.getDescription()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("티켓 폼 수정 테스트")
    class DescribeUpdateTicketForm {

        @Test
        @DisplayName("존재하는 티켓 폼의 설명을 변경하면 정상적으로 변경된다.")
        void should_UpdateTicketFormDescription_when_FormExists() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;
            String description = "Ticket Form Test";

            Category secondCategory = mock(Category.class);
            TicketForm ticketForm = mock(TicketForm.class);

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.getParent()).thenReturn(mock(Category.class));
            when(secondCategory.getParent().getId()).thenReturn(firstCategoryId);

            TicketFormId formId = new TicketFormId(firstCategoryId, secondCategoryId);
            when(ticketFormRepository.findById(formId))
                    .thenReturn(Optional.of(ticketForm));

            // when
            ticketFormService.updateTicketForm(firstCategoryId, secondCategoryId, description);

            // then
            verify(ticketForm, times(1)).updateDescription(description);
        }

        @Test
        @DisplayName("2차 카테고리가 존재하지 않으면 오류가 발생한다.")
        void should_ThrowException_when_SecondCategoryDoesNotExist() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 999L;
            String description = "Ticket Form Test";

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketFormService.updateTicketForm(firstCategoryId, secondCategoryId, description))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("2차 카테고리가 1차 카테고리 하위 카테고리가 아니라면 오류가 발생한다.")
        void should_ThrowException_when_SecondCategoryIsNotChildOfFirstCategory() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;
            String description = "Ticket Form Test";

            Category secondCategory = mock(Category.class);

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.getParent()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> ticketFormService.updateTicketForm(firstCategoryId, secondCategoryId, description))
                    .isInstanceOf(InvalidCategoryLevelException.class);
        }

        @Test
        @DisplayName("티켓 폼이 존재하지 않으면 오류가 발생한다.")
        void should_ThrowException_when_FormDoesNotExist() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;
            String description = "Ticket Form Test";

            Category secondCategory = mock(Category.class);

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.getParent()).thenReturn(mock(Category.class));
            when(secondCategory.getParent().getId()).thenReturn(firstCategoryId);

            TicketFormId formId = new TicketFormId(firstCategoryId, secondCategoryId);
            when(ticketFormRepository.findById(formId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketFormService.updateTicketForm(firstCategoryId, secondCategoryId, description))
                    .isInstanceOf(TicketFormNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("티켓 폼 삭제 테스트")
    class DescribeDeleteTicketForm {

        @Test
        @DisplayName("티켓 폼이 존재하면 정상적으로 티켓 폼을 삭제한다.")
        void should_DeleteTicketForm_when_FormExists() {
            // given
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

            // when
            ticketFormService.deleteTicketForm(firstCategoryId, secondCategoryId);

            // then
            verify(ticketFormRepository, times(1)).delete(ticketForm);
        }

        @Test
        @DisplayName("2차 카테고리가 존재하지 않으면 오류가 발생한다.")
        void should_ThrowException_when_SecondCategoryDoesNotExist() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 999L;

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketFormService.deleteTicketForm(firstCategoryId, secondCategoryId))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("2차 카테고리가 1차 카테고리 하위 카테고리가 아니라면 오류가 발생한다.")
        void should_ThrowException_when_SecondCategoryIsNotChildOfFirstCategory() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;

            Category secondCategory = mock(Category.class);

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.getParent()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> ticketFormService.deleteTicketForm(firstCategoryId, secondCategoryId))
                    .isInstanceOf(InvalidCategoryLevelException.class);
        }

        @Test
        @DisplayName("티켓 폼이 존재하지 않으면 오류가 발생한다.")
        void should_ThrowException_when_FormDoesNotExist() {
            // given
            Long firstCategoryId = 1L;
            Long secondCategoryId = 2L;

            Category secondCategory = mock(Category.class);

            when(categoryRepository.findById(secondCategoryId))
                    .thenReturn(Optional.of(secondCategory));
            when(secondCategory.getParent()).thenReturn(mock(Category.class));
            when(secondCategory.getParent().getId()).thenReturn(firstCategoryId);

            TicketFormId formId = new TicketFormId(firstCategoryId, secondCategoryId);
            when(ticketFormRepository.findById(formId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketFormService.deleteTicketForm(firstCategoryId, secondCategoryId))
                    .isInstanceOf(TicketFormNotFoundException.class);
        }
    }
}
