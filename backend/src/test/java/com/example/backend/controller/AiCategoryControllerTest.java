package com.example.backend.controller;

import com.example.backend.application.service.AiCategoryService;
import com.example.backend.generated.model.CategoryPredictionRequest;
import com.example.backend.generated.model.CategoryPredictionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AiCategoryControllerのユニットテストクラス
 * 入力検証とサービス呼び出しの境界を中心に確認する。
 */
@ExtendWith(MockitoExtension.class)
class AiCategoryControllerTest {

    @Mock
    private AiCategoryService aiCategoryService;

    @InjectMocks
    private AiCategoryController aiCategoryController;

    @Nested
    @DisplayName("apiAiCategoryPost")
    class ApiAiCategoryPost {

        @Test
        @DisplayName("説明文が有効なとき、推論カテゴリを200で返す")
        void returnsOkWithPredictedCategory() {
            CategoryPredictionRequest request = new CategoryPredictionRequest();
            request.setDescription("コンビニでおにぎりを購入");
            when(aiCategoryService.predictCategory("コンビニでおにぎりを購入")).thenReturn("食費");

            ResponseEntity<CategoryPredictionResponse> response = aiCategoryController.apiAiCategoryPost(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCategory()).isEqualTo("食費");
            verify(aiCategoryService).predictCategory("コンビニでおにぎりを購入");
        }

        @Test
        @DisplayName("説明文が空文字のとき、IllegalArgumentExceptionを送出する")
        void throwsWhenDescriptionIsEmpty() {
            CategoryPredictionRequest request = new CategoryPredictionRequest();
            request.setDescription("");

            assertThatThrownBy(() -> aiCategoryController.apiAiCategoryPost(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("説明文が空白です。");
            verify(aiCategoryService, never()).predictCategory(request.getDescription());
        }
    }
}
