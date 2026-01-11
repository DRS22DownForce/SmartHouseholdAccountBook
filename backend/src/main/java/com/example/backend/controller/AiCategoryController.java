package com.example.backend.controller;

import com.example.backend.application.service.AiCategoryService;
import com.example.backend.generated.api.AiApi;
import com.example.backend.generated.model.CategoryPredictionRequest;
import com.example.backend.generated.model.CategoryPredictionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * AIカテゴリー自動分類に関するREST APIコントローラー
 * 
 * このコントローラーは支出の説明文から、AIを使用して適切なカテゴリーを自動分類する機能を提供します。
 * OpenAPI Generatorで生成されたAiApiインターフェースを実装しています。
 */
@RestController
public class AiCategoryController implements AiApi {

    private final AiCategoryService aiCategoryService;

    /**
     * コンストラクタ
     * 
     * @param aiCategoryService AIカテゴリー自動分類サービス
     */
    public AiCategoryController(AiCategoryService aiCategoryService) {
        this.aiCategoryService = aiCategoryService;
    }

    /**
     * AIカテゴリー自動分類API
     * 
     * 支出の説明文を受け取り、AIが適切なカテゴリーを推論して返します。
     * エラーハンドリングはGlobalExceptionHandlerで統一処理されます。
     * 
     * @param categoryPredictionRequest カテゴリー推論リクエスト（説明文を含む）
     * @return カテゴリー推論レスポンス（推論されたカテゴリー名を含む）
     */
    @Override
    public ResponseEntity<CategoryPredictionResponse> apiAiCategoryPost(
            CategoryPredictionRequest categoryPredictionRequest) {
        // サービス層を呼び出してカテゴリーを推論
        // エラーが発生した場合は、GlobalExceptionHandlerで処理される
        String predictedCategory = aiCategoryService.predictCategory(
                categoryPredictionRequest.getDescription());

        // レスポンスオブジェクトを作成
        CategoryPredictionResponse response = new CategoryPredictionResponse();
        response.setCategory(predictedCategory);

        return ResponseEntity.ok(response);
    }
}
