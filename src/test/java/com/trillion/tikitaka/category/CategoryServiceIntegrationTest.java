package com.trillion.tikitaka.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.category.dto.request.CategoryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("카테고리 통합 테스트")
public class CategoryServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("카테고리 생성")
    class DescribeCreateCategory {

        @Test
        @DisplayName("상위 카테고리가 주어지지 않은 경우 1차 카테고리를 생성한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_CreatePrimaryCategory_when_NoParentCategory() throws Exception {
            // given
            CategoryRequest request = new CategoryRequest("테스트 1차 카테고리");

            // when & then
            mockMvc.perform(post("/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());

            String response = mockMvc.perform(get("/categories/list")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            assertThat(response).contains("테스트 1차 카테고리");
        }

        @Test
        @DisplayName("상위 카테고리가 주어진 경우 그 하위에 2차 카테고리를 생성한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_CreateSecondaryCategory_when_ParentCategoryProvided() throws Exception {
            // given
            CategoryRequest primaryRequest = new CategoryRequest("부모 카테고리");
            mockMvc.perform(post("/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(primaryRequest)))
                    .andExpect(status().isOk());


            String listResponse = mockMvc.perform(get("/categories/list")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            assertThat(listResponse).contains("부모 카테고리");
            var root = objectMapper.readTree(listResponse).get("data");
            Long parentId = null;
            for (var node : root) {
                if ("부모 카테고리".equals(node.get("name").asText())) {
                    parentId = node.get("id").asLong();
                    break;
                }
            }
            assertThat(parentId).isNotNull();

            // when
            CategoryRequest secondaryRequest = new CategoryRequest("자식 카테고리");

            mockMvc.perform(post("/categories")
                            .param("parentId", String.valueOf(parentId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondaryRequest)))
                    .andExpect(status().isOk());

            String childListResponse = mockMvc.perform(get("/categories/list")
                            .param("parentId", String.valueOf(parentId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // then
            assertThat(childListResponse).contains("자식 카테고리");
        }

        @Test
        @DisplayName("중복되는 카테고리명으로 생성 요청 시 에러를 반환한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_FailToCreateCategory_when_DuplicateCategoryName() throws Exception {
            // given
            CategoryRequest request = new CategoryRequest("중복 카테고리");
            mockMvc.perform(post("/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // when & then
            mockMvc.perform(post("/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("카테고리 조회")
    class DescribeGetCategory {

        @Test
        @DisplayName("별도의 1차 카테고리 ID가 주어지지 않은 경우 1차 카테고리 목록을 반환한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_ReturnPrimaryCategories_when_NoParentId() throws Exception {
            // given
            CategoryRequest request = new CategoryRequest("조회용 1차 카테고리");
            mockMvc.perform(post("/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // when & then
            mockMvc.perform(get("/categories/list")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[?(@.name=='조회용 1차 카테고리')]").exists());
        }

        @Test
        @DisplayName("1차 카테고리 ID가 주어진 경우 해당 카테고리의 2차 카테고리 목록을 반환한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_FailToReturnSecondaryCategories_when_InvalidParentId() throws Exception {
            // when & then
            mockMvc.perform(get("/categories/list")
                            .param("parentId", "9999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("카테고리 수정")
    class DescribeUpdateCategory {

        @Test
        @DisplayName("유효한 카테고리 ID와 중복되지 않는 카테고리명이 주어진 경우 카테고리를 수정한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_UpdateCategory_when_ValidCategoryIdAndNoDuplicateName() throws Exception {
            // given
            CategoryRequest createRequest = new CategoryRequest("수정 전 카테고리");
            mockMvc.perform(post("/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk());


            String listResponse = mockMvc.perform(get("/categories/list")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            var root = objectMapper.readTree(listResponse).get("data");
            Long categoryId = null;
            for (var node : root) {
                if ("수정 전 카테고리".equals(node.get("name").asText())) {
                    categoryId = node.get("id").asLong();
                    break;
                }
            }
            assertThat(categoryId).isNotNull();

            // when
            CategoryRequest updateRequest = new CategoryRequest("수정 후 카테고리");

            mockMvc.perform(patch("/categories/" + categoryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk());

            String afterUpdateResponse = mockMvc.perform(get("/categories/list")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // then
            assertThat(afterUpdateResponse).contains("수정 후 카테고리");
        }

        @Test
        @DisplayName("카테고리가 존재하지 않는 경우 수정에 실패한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_FailToUpdateCategory_when_CategoryDoesNotExist() throws Exception {
            // given
            CategoryRequest updateRequest = new CategoryRequest("수정");

            // when & then
            mockMvc.perform(patch("/categories/9999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("카테고리 삭제")
    class DescribeDeleteCategory {

        @Test
        @DisplayName("카테고리가 존재하는 경우 해당 카테고리와 하위 카테고리를 모두 삭제한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_DeleteCategoryAndChildren_when_CategoryExists() throws Exception {
            // given
            CategoryRequest primaryRequest = new CategoryRequest("삭제용 1차 카테고리");
            mockMvc.perform(post("/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(primaryRequest)))
                    .andExpect(status().isOk());

            String listResponse = mockMvc.perform(get("/categories/list")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            var root = objectMapper.readTree(listResponse).get("data");
            Long parentId = null;
            for (var node : root) {
                if ("삭제용 1차 카테고리".equals(node.get("name").asText())) {
                    parentId = node.get("id").asLong();
                    break;
                }
            }
            assertThat(parentId).isNotNull();

            // when
            CategoryRequest secondaryRequest = new CategoryRequest("삭제용 2차 카테고리");
            mockMvc.perform(post("/categories")
                            .param("parentId", String.valueOf(parentId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondaryRequest)))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/categories/" + parentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // then
            mockMvc.perform(get("/categories/list")
                            .param("parentId", String.valueOf(parentId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("카테고리가 존재하지 않는 경우 삭제에 실패한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_FailToDeleteCategory_when_CategoryDoesNotExist() throws Exception {
            // when & then
            mockMvc.perform(delete("/categories/9999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
