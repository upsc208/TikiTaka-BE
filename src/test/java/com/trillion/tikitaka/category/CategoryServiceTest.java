package com.trillion.tikitaka.category;

import com.trillion.tikitaka.category.application.CategoryService;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.dto.request.CategoryRequest;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.DuplicatedCategoryException;
import com.trillion.tikitaka.category.exception.PrimaryCategoryNotFoundException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("카테고리 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("카테고리 생성 테스트")
    class DescribeCreateCategory {

        @Test
        @DisplayName("상위 카테고리를 지정하지 않고 중복되는 카테고리명이 없으면 1차 카테고리를 생성한다.")
        void should_CreatePrimaryCategory_when_NoDuplicateNameAndNoParentId() {
            // given
            CategoryRequest request = new CategoryRequest("테스트 카테고리");

            when(categoryRepository.findByName(request.getName())).thenReturn(Optional.empty());

            // when
            categoryService.createCategory(null, request);

            // then
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("상위 카테고리를 지정하고 중복되는 카테고리명이 없으면 2차 카테고리를 생성한다.")
        void should_CreateSecondaryCategory_when_NoDuplicateNameAndParentId() {
            // given
            Long parentId = 1L;
            CategoryRequest request = new CategoryRequest("테스트 카테고리");

            Category parentCategory = mock(Category.class);
            when(categoryRepository.findByName(request.getName())).thenReturn(Optional.empty());
            when(categoryRepository.findByIdAndParentIsNull(parentId)).thenReturn(Optional.of(parentCategory));

            // when
            categoryService.createCategory(parentId, request);

            // then
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("동일한 카테고리명이 존재하면 오류가 발생한다.")
        void should_ThrowException_when_DuplicateName() {
            // given
            CategoryRequest request = new CategoryRequest("테스트 카테고리");

            Category existingCategory = mock(Category.class);
            when(categoryRepository.findByName(request.getName())).thenReturn(Optional.of(existingCategory));

            // when & then
            assertThatThrownBy(() -> categoryService.createCategory(null, request)).
                    isInstanceOf(DuplicatedCategoryException.class);
        }

        @Test
        @DisplayName("지정한 상위 카테고리가 1차 카테고리가 아니면 오류가 발생한다.")
        void should_ThrowException_when_ParentCategoryIsNotPrimary() {
            // given
            Long parentId = 1L;
            CategoryRequest request = new CategoryRequest("테스트 카테고리");

            when(categoryRepository.findByName(request.getName())).thenReturn(Optional.empty());
            when(categoryRepository.findByIdAndParentIsNull(parentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> categoryService.createCategory(parentId, request)).
                    isInstanceOf(PrimaryCategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("카테고리 조회 테스트")
    class DescribeGetCategories {

        @Test
        @DisplayName("부모 카테고리가 주어지지 않으면 모든 1차 카테고리를 조회한다.")
        void should_ReturnAllFirstCategories_when_NoParentId() {
            // given
            List<CategoryResponse> mockList = List.of(
                    new CategoryResponse(1L, "Primary1", null),
                    new CategoryResponse(2L, "Primary2", null)
            );

            when(categoryRepository.getCategories(null)).thenReturn(mockList);

            // when
            List<CategoryResponse> categories = categoryService.getCategories(null);

            // then
            assertThat(categories).hasSize(2);
            assertThat(categories.get(0).getName()).isEqualTo("Primary1");
            assertThat(categories.get(1).getName()).isEqualTo("Primary2");
        }

        @Test
        @DisplayName("부모 카테고리가 주어지면 해당 1차 카테고리의 모든 2차 카테고리를 조회한다.")
        void should_ReturnAllSecondCategories_when_ParentIdValid() {
            // given
            Long parentId = 1L;
            List<CategoryResponse> mockList = List.of(
                    new CategoryResponse(3L, "Secondary1", 1L),
                    new CategoryResponse(4L, "Secondary2", 1L)
            );

            Category parentCategory = mock(Category.class);
            when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parentCategory));
            when(categoryRepository.getCategories(parentId)).thenReturn(mockList);

            // when
            List<CategoryResponse> categories = categoryService.getCategories(parentId);

            // then
            assertThat(categories).hasSize(2);
            assertThat(categories.get(0).getName()).isEqualTo("Secondary1");
            assertThat(categories.get(1).getName()).isEqualTo("Secondary2");
        }

        @Test
        @DisplayName("카테고리가 없으면 빈 목록을 반환한다.")
        void should_ReturnEmptyList_when_NoCategoryExist() {
            // given
            when(categoryRepository.getCategories(any())).thenReturn(List.of());

            // when
            List<CategoryResponse> categories = categoryService.getCategories(null);

            // then
            assertThat(categories).isEmpty();
        }

        @Test
        @DisplayName("2차 카테고리 조회 시 1차 카테고리가 존재하지 않으면 오류가 발생한다.")
        void should_ThrowException_when_ParentIdInvalid() {
            // given
            Long parentId = 999L;

            when(categoryRepository.findById(parentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> categoryService.getCategories(parentId))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("카테고리 수정 테스트")
    class DescribeUpdateCategory {

        @Test
        @DisplayName("수정할 카테고리가 존재하고 중복되는 카테고리명이 없으면 카테고리명을 수정한다.")
        void should_UpdateCategory_when_ValidCategoryIdAndNoDuplicateName() {
            // given
            Long categoryId = 1L;
            CategoryRequest request = new CategoryRequest("수정된 카테고리");

            Category existingCategory = new Category("기존 카테고리", null);
            ReflectionTestUtils.setField(existingCategory, "id", categoryId);

            when(categoryRepository.findByIdOrName(categoryId, request.getName())).thenReturn(List.of(existingCategory));

            // when
            categoryService.updateCategory(categoryId, request);

            // then
            assertThat(existingCategory.getName()).isEqualTo("수정된 카테고리");
        }

        @Test
        @DisplayName("수정하려는 카테고리가 존재하지 않으면 오류가 발생한다.")
        void should_ThrowException_when_CategoryDoesNotExist() {
            // given
            Long categoryId = 999L;
            CategoryRequest request = new CategoryRequest("카테고리명");

            when(categoryRepository.findByIdOrName(categoryId, request.getName())).thenReturn(List.of());

            // when & then
            assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("이미 동일한 이름을 가진 카테고리가 존재하면 오류가 발생한다.")
        void should_ThrowException_when_CategoryNameAlreadyExists() {
            // given
            Long categoryId = 1L;
            CategoryRequest request = new CategoryRequest("기존 카테고리");

            Category catWithSameName = new Category("기존 카테고리", null);
            ReflectionTestUtils.setField(catWithSameName, "id", 2L);

            Category catWithTargetId = new Category("수정된 카테고리", null);
            ReflectionTestUtils.setField(catWithTargetId, "id", categoryId);

            when(categoryRepository.findByIdOrName(categoryId, request.getName()))
                    .thenReturn(List.of(catWithSameName, catWithTargetId));

            // when & then
            assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request))
                    .isInstanceOf(DuplicatedCategoryException.class);
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 테스트")
    class DescribeDeleteCategory {

        @Test
        @DisplayName("자식 카테고리가 있다면 자식들을 포함해 제거한다.")
        void should_SoftDeleteCategoryAndChildren_when_CategoryExists() {
            // given
            Long categoryId = 1L;
            Category parentCategory = new Category("Parent", null);

            Category child1 = new Category("Child1", parentCategory);
            Category child2 = new Category("Child2", parentCategory);
            parentCategory.getChildren().add(child1);
            parentCategory.getChildren().add(child2);

            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.of(parentCategory));

            // when
            categoryService.deleteCategory(categoryId);

            // then
            verify(categoryRepository, times(1)).deleteAll(parentCategory.getChildren());
        }

        @Test
        @DisplayName("카테고리가 존재하지 않으면 예외가 발생한다.")
        void should_ThrowException_when_CategoryDoesNotExist() {
            // given
            Long categoryId = 999L;
            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }
}
